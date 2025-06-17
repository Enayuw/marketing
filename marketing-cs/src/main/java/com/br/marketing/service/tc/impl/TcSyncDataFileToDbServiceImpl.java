package com.br.marketing.service.tc.impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.service.tc.TcSyncDataFileToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private final static String TITLE = "【同程易融-fileToDbShard任务】";

    private Integer PAGE_SIZE = 5000;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Autowired
    RedisChgService redisChgService;


    @Override
    public void shardProcess(String apiCode,List<Integer> shardingItems) {
        for (;;) {
            if (!marketingCommonConfig.getTcTxtFileShardConfig().getBoolean("jobSwitch")) {
                break;
            }
            // 对txt文件名加锁->获取锁成功继续...->获取锁失败->continue下一个循环
            String lockKey = RedisKeyConstant.prefix.concat("tcyr_sync:txtToDb:").concat(apiCode);
            String lockValue = UUID.randomUUID().toString();
            try {
                //1、抢锁
                redisChgService.lock(lockKey, lockValue);
                // 2、查询单条未处理的txt
                MarketingTcyrSyncFile tcyrSyncFile = tcyrSyncFileMapper.selectSingleSyncFile(apiCode,0);
                if (ObjectUtil.isEmpty(tcyrSyncFile)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3、修改txt处理状态
                tcyrSyncFile.setDealStatus(1);
                tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
                redisChgService.unlock(lockKey, lockValue);
                //4、处理txt数据入库
                parseCsvFileToDb(tcyrSyncFile.getId(), tcyrSyncFile.getApiCode(), tcyrSyncFile.getBatchNo(),
                        tcyrSyncFile.getFileName(),tcyrSyncFile.getFilePath(),shardingItems);
            }catch (Exception e) {
                // TODO 单个txt处理异常时 是抛出异常终止任务 or 继续进行 (异常时候break跳出循环 or 继续执行下一个文件处理)
                // 5、异常时释放锁(finally)
                redisChgService.unlock(lockKey, lockValue);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        e.getMessage(), TITLE), e);
                if (!marketingCommonConfig.getTcMatchShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
            }
        }
    }

    private void parseCsvFileToDb(Long syncFileId, String apiCode, String batchNo, String fileName, String filePath,List<Integer> shardingItems) {
        log.warn("TITLE:{} csv文件入db,syncFileId{},batchNo:{},csvName:{},分片:{},txtToDb开始执行...",
                TITLE,syncFileId,batchNo,fileName,shardingItems);
        Long start = System.currentTimeMillis();
        MarketingTcyrSyncFile syncFileItem = tcyrSyncFileMapper.selectByPrimaryKey(syncFileId);
        // 0、前置校验下id对应数据是否存在、文件处理状态是否正常(0:未处理 1:处理中 2:处理完成)
        if (ObjectUtil.isEmpty(syncFileItem) || (syncFileItem!=null && syncFileItem.getDealStatus()!=1)) {
            log.warn("TITLE:{} csv文件入db完成,id:{},batchNo:{},csvName:{},分片:{}, 文件记录状态异常",
                    TITLE,syncFileId,batchNo,fileName,shardingItems);
            return;
        }
        // 1、判断文件存在
        File txtFile = new File(filePath);
        if (!txtFile.exists()) {
            log.warn("TITLE:{} csv文件入db,batchNo:{},csvName:{},csvPath:{} 文件不存在",TITLE,batchNo,fileName,filePath);
        }
        // 2、txt文件解析入库
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            //2.1批处理数据
            List<String> lineBuffer = new ArrayList<>();
            ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                    marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"),
                    marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool")
            );
            String line;
            while ((line = reader.readLine()) != null) {
                PAGE_SIZE = marketingCommonConfig.getTcTxtFileShardConfig().getInteger("pageSize");
                lineBuffer.add(line);
                if (lineBuffer.size() >= PAGE_SIZE) {
                    List<String> lineList = new ArrayList<>();
                    lineList.addAll(lineBuffer);
                    processList(syncFileId,apiCode,batchNo,lineList,actionPool);
                    lineBuffer.clear();
                }
            }
            //2.2处理剩余数据
            if (!lineBuffer.isEmpty()) {
                List<String> lineList = new ArrayList<>();
                lineList.addAll(lineBuffer);
                processList(syncFileId,apiCode,batchNo,lineList,actionPool);
            }
            //3、修改txt完成状态、总成功条数
            syncFileItem.setDealStatus(2);
            Long totalLine = Files.lines(Paths.get(filePath)).count();
            syncFileItem.setTotalCount(totalLine);
            tcyrSyncFileMapper.updateByPrimaryKey(syncFileItem);
            log.warn("TITLE:{} csv文件入db完成,syncFileId:{},batchNo:{},csvName:{},分片:{},totalCount:{},successCount:{},执行时间:{}",
                    TITLE,syncFileId,batchNo,fileName,shardingItems,totalLine,totalLine,System.currentTimeMillis()-start);
            shutdownThreadPool(actionPool);
        } catch (IOException e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }


    private void processList(Long syncFileId,String apiCode, String batchNo, List<String> lineList, ThreadPoolExecutor actionPool) {
        actionPool.setCorePoolSize(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
        lineList.forEach(line -> {
            CompletableFuture.runAsync(() -> processSingleLineData(syncFileId,apiCode, batchNo,line), actionPool);
        });
    }

    private void processSingleLineData(Long syncFileId,String apiCode, String batchNo,String line) {
        String[] data = line.split(",");
        String userKey;
        int terminal = 0;
        int dataStatus = 0;
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
        //TODO  b_marketing_tcyr_sync 新增syncFileId字段(最后统计txt成功的条数)
        syncItem.setSyncFileId(syncFileId);
        tcyrSyncMapper.insertSelective(syncItem);
    }

    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
//        log.warn(TITLE + "shutdownThreadPool开始");
//        long taskCount = -1;
//        executor.shutdown();
//        try {
//            while (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
//                long completedTaskCount = executor.getCompletedTaskCount();
//                if (taskCount == completedTaskCount) {
//                    log.warn(TITLE + "业务线程等待超时");
//                    break;
//                }
//                taskCount = completedTaskCount;
//            }
//        } catch (InterruptedException e) {
//            executor.shutdownNow();
//            Thread.interrupted();
//        } catch (Throwable e) {
//            log.warn(TITLE + "ThreadPoolManager shutdown executor has error : ", e);
//        }
//        log.warn(TITLE + "shutdownThreadPool结束");

        log.info(TITLE + "shutdownThreadPool开始");
        executor.shutdown(); // 停止接收新任务
        long totalTimeout = 30; // 最大等待30分钟
        long elapsed = 0;
        long lastCompletedCount = -1;
        try {
            while (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
                elapsed += 2;
                long currentCompleted = executor.getCompletedTaskCount();
                // 总超时检查
                if (elapsed >= totalTimeout) {
                    log.warn("❗ 达到总超时时间，强制终止");
                    List<Runnable> unfinished = executor.shutdownNow();
                    log.warn("未完成任务: {}", unfinished.size());
                    break;
                }

                // 任务停滞检查
                if (lastCompletedCount == currentCompleted) {
                    log.warn("⚠️ 任务2分钟内无进展，强制终止");
                    List<Runnable> unfinished = executor.shutdownNow();
                    log.warn("未完成任务: {}", unfinished.size());
                    break;
                }

                lastCompletedCount = currentCompleted;
                log.info("等待中... 已完成: {}/{}",
                        currentCompleted, executor.getTaskCount());
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            log.info(TITLE + "shutdownThreadPool结束");
        }
    }
}
