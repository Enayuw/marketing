package com.br.marketing.service.tc.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataDownFileService;
import com.br.marketing.service.tc.TcSyncDataFileToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 同城易融拉取GZ文件 TXT信息入库-Service实现
 *
 * @author zhiyong.zhang
 * @date 2024/04/21
 */
@Service
@Slf4j
public class TcSyncDataFileToDbServiceImpl implements TcSyncDataFileToDbService {

    private final static String TITLE = "【同程易融-DownFile任务】";

    private Integer PARTITION_SIZE = 1000;

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncRecordMapper tcPullGzFileMapper;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Autowired
    RedisChgService redisChgService;


    @Override
    public void process(String apiCode,List<Integer> shardingItems) {
        for (;;) {
            if (!marketingCommonConfig.getTcTxtFileShardConfig().getBoolean("jobSwitch")) {
                break;
            }
            // 1、查询单条未处理的txt
            MarketingTcyrSyncFile tcyrSyncFile = tcyrSyncFileMapper.selectSyncFile(apiCode,0);
            if (ObjectUtil.isEmpty(tcyrSyncFile)) {
                break;
            }
            String lockKey = RedisKeyConstant.prefix.concat("tcyr_sync:").
                    concat(apiCode).concat(":").concat(tcyrSyncFile.getBatchNo()+"_"+tcyrSyncFile.getFileName());
            String lockValue = UUID.randomUUID().toString();
            try {
                // 2.对txt文件名加锁(->2.1获取锁成功继续... 2.2获取锁失败 continue下一个循环)
                //2.1抢锁
                redisChgService.lock(lockKey, lockValue);
                //2.2处理txt数据入库
                parseCsvFileToDb(tcyrSyncFile.getId(), tcyrSyncFile.getApiCode(), tcyrSyncFile.getBatchNo(),
                        tcyrSyncFile.getFileName(),tcyrSyncFile.getFilePath(),shardingItems);
                //2.3释放锁
                redisChgService.unlock(lockKey, lockValue);
            }catch (Exception e) {
                // TODO 单个txt处理异常时 是抛出异常终止任务 or 继续进行
                // 4、释放锁(finally)
                redisChgService.unlock(lockKey, lockValue);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        e.getMessage(), TITLE), e);
            }
        }
    }

    private void parseCsvFileToDb(Long id, String apiCode, String batchNo, String fileName, String filePath,List<Integer> shardingItems) {
        Long start = System.currentTimeMillis();
        log.warn("TITLE:{} csv文件入db,batchNo:{},csvName:{},分片:{} 开始执行", TITLE,batchNo,fileName,shardingItems);
        // 1、(第一步查询极端情况查到同一个节点)--> 重新获取db数据, deal_status=1  表明其它线程正在处理中，则返回，执行下一个文件
        MarketingTcyrSyncFile syncFileItem = tcyrSyncFileMapper.selectByPrimaryKey(id);
        if (!ObjectUtil.isEmpty(syncFileItem) && syncFileItem.getDealStatus() == 1) {
            log.warn("TITLE:{} csv文件入db,batchNo:{},csvName:{} 已有线程在处理...",TITLE,batchNo,fileName);
        }
        syncFileItem.setDealStatus(1);
        tcyrSyncFileMapper.updateByPrimaryKey(syncFileItem);

        // 2、判断文件存在
        File txtFile = new File(filePath);
        if (!txtFile.exists()) {
            log.warn("TITLE:{} csv文件入db,batchNo:{},csvName:{},csvPath:{} 文件不存在",TITLE,batchNo,fileName,filePath);
        }
        // 3、txt文件解析入库
        Long successLine = 0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            //3.1批处理数据
            List<String> lineBuffer = new ArrayList<>();
            ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                    marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"),
                    marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool")
            );
            List<CompletableFuture<Result>> futureList = new ArrayList<>();
            List<Long> resultList = Collections.synchronizedList(new ArrayList<>(20));
            String line;
            Long totalLine = 0L;
            while ((line = reader.readLine()) != null) {
                PARTITION_SIZE = marketingCommonConfig.getTcTxtFileShardConfig().getInteger("partSize");
                lineBuffer.add(line);
                if (lineBuffer.size() >= PARTITION_SIZE) {
                    List<String> lineList = new ArrayList<>();
                    lineList.addAll(lineBuffer);
                    totalLine += lineBuffer.size();
                    processList(apiCode,batchNo,lineList,actionPool,futureList,resultList);
                    lineBuffer.clear();
                }
            }
            // 处理剩余数据
            if (!lineBuffer.isEmpty()) {
                List<String> lineList = new ArrayList<>();
                lineList.addAll(lineBuffer);
                totalLine += lineBuffer.size();
                processList(apiCode,batchNo,lineList,actionPool,futureList,resultList);
            }
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            for (Long successCount : resultList) {
                successLine += successCount;
            }
            //4、修改txt完成状态、总成功条数
            syncFileItem.setDealStatus(2);
            syncFileItem.setTotalCount(totalLine);
            tcyrSyncFileMapper.updateByPrimaryKey(syncFileItem);
            shutdownThreadPool(actionPool);
            log.warn("TITLE:{} csv文件入db完成,batchNo:{},csvName:{},分片:{},totalCount:{},successCount:{},执行时间:{}",
                    TITLE,batchNo,fileName,shardingItems,totalLine,successLine,System.currentTimeMillis()-start);
        } catch (IOException e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }


    private Result processList(String apiCode, String batchNo, List<String> lineList, ThreadPoolExecutor actionPool,
                               List<CompletableFuture<Result>> futureList, List<Long> resultList) {
        Result result = new Result().failure();
        actionPool.setCorePoolSize(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode, batchNo, lineList), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add((Long) processDataResult.getData());
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),throwable.getMessage(), TITLE), throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result.success();
    }

    private Result processData(String apiCode, String batchNo, List<String> lineList) {
        Result result = new Result().failure();
        try {
            Result processResult = processLineBuffer(apiCode, batchNo, lineList);
            if (processResult == null || !processResult.isSuccess() || processResult.getData() == null) {
                return result.failure();
            }
            List<MarketingTcyrSync> dataList = (List<MarketingTcyrSync>) processResult.getData();
            if (CollectionUtils.isEmpty(dataList)) {
                return result.failure();
            }
            tcPullGzFileMapper.batchAdd(dataList);
            return result.success().setDate( Long.valueOf(dataList.size()));
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            return result.failure();
        }
    }

    /**
     * 转化成 List<MarketingTcyrSync>
     * @param apiCode
     * @param batchNo
     * @param lineList
     * @return
     */
    private Result processLineBuffer(String apiCode, String batchNo, List<String> lineList) {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(lineList)) {
            return result.success();
        }
        List<MarketingTcyrSync> dataList = new ArrayList<>();
        List<String> userKeyList = new ArrayList<>();
        for (String line : lineList) {
            String[] data = line.split(",");
            String userKey;
            Integer terminal = 0;
            Integer dataStatus = 0;
            if (data.length > 1) {
                String firstColumn = data[0].trim();
                String secondColumn = data[1].trim();
                // 单个字段为空写入，数据状态异常；整行为空，也存入
                if (StringUtils.isNotBlank(firstColumn) && StringUtils.isNotBlank(secondColumn)) {
                    dataStatus = 1;
                }
                userKey = firstColumn;
                if (StringUtils.isNotBlank(secondColumn)) {
                    try {
                        terminal =Integer.parseInt(secondColumn);
                    }catch (Exception e) {
                        log.warn("{},porcessLine解析terminal异常,{},e:",TITLE,secondColumn,e);
                        terminal =-2;
                    }
                }else {
                    terminal = -1;
                }
            }else {
                userKey= "";
                terminal = -1;
            }
            MarketingTcyrSync syncItem = new MarketingTcyrSync();
            syncItem.setApiCode(apiCode);
            syncItem.setBatchNo(batchNo);
            syncItem.setUserKey(userKey);
            syncItem.setTerminal(terminal);
            Date nowDate = new Date();
            syncItem.setCreateTime(nowDate);
            syncItem.setUpdateTime(nowDate);
            syncItem.setStatus(dataStatus);
            userKeyList.add(userKey);
            dataList.add(syncItem);
        }
        return result.success().setDate(dataList);
    }

    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        long taskCount = -1;
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                long completedTaskCount = executor.getCompletedTaskCount();
                if (taskCount == completedTaskCount) {
                    log.warn(TITLE + "业务线程等待超时");
                    break;
                }
                taskCount = completedTaskCount;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (Throwable e) {
            log.warn(TITLE + "ThreadPoolManager shutdown executor has error : ", e);
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }
}
