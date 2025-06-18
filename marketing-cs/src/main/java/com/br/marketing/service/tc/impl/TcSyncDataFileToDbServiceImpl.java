package com.br.marketing.service.tc.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
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

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Autowired
    RedisChgService redisChgService;

    // todo 技术方案 详细
    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrSyncTxtToDb.concat(apiCode);;
        String lockValue = "";
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"),
                marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
        try {
            for (;;) {
                if (!marketingCommonConfig.getTcTxtFileShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                //todo 抢锁超时 如何处理 ->重试
                lockValue = UUID.randomUUID().toString();
                //1、抢锁
                redisChgService.lock(lockKey, lockValue);
                // 2、查询单条未处理的txt
                MarketingTcyrSyncFile tcyrSyncFile = tcyrSyncFileMapper.selectSingleSyncFile(apiCode, 0);
                if (ObjectUtil.isEmpty(tcyrSyncFile)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3、修改txt处理状态
                tcyrSyncFile.setDealStatus(1);
                tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
                redisChgService.unlock(lockKey, lockValue);
                //4、处理txt数据入库
                parseCsvFileToDb(tcyrSyncFile,actionPool);
            }
            Thread.sleep(30000);
            //4、计算dbCount
            List<MarketingTcyrSyncFile> syncFileList = tcyrSyncFileMapper.selectSyncFileList(apiCode,2);
            syncFileList.forEach(tcyrSyncFile -> {
                Long dbCount = tcyrSyncMapper.selecFileDbCount(tcyrSyncFile.getApiCode(),tcyrSyncFile.getId());
                tcyrSyncFile.setSuccessCount(dbCount);
                tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
            });
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            //5、异常时释放锁(finally)
            redisChgService.unlock(lockKey, lockValue);
            shutdownThreadPool(actionPool);
        }
    }


    private void parseCsvFileToDb(MarketingTcyrSyncFile tcyrSyncFile, ThreadPoolExecutor actionPool) {
        Long start = System.currentTimeMillis();
        // 1、判断文件存在
        File txtFile = new File(tcyrSyncFile.getFilePath());
        if (!txtFile.exists()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    tcyrSyncFile.getId()+"文件不存在", TITLE));
            return;
        }
        // 2、txt文件解析入库
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            Long totalCount = 0L;
            String line;
            while ((line = reader.readLine()) != null) {
                actionPool.setCorePoolSize(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
                actionPool.setMaximumPoolSize(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
                String finalLine = line;
                CompletableFuture.runAsync(() -> processSingleLineData(tcyrSyncFile.getId(),
                        tcyrSyncFile.getApiCode(), tcyrSyncFile.getBatchNo(), finalLine), actionPool);
                totalCount ++;
            }
            //3、修改txt完成状态、总成功条数
            tcyrSyncFile.setDealStatus(2);
            tcyrSyncFile.setTotalCount(totalCount);
            tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
            log.warn("TITLE:{} csv文件入db完成,syncFileId:{},csvName:{},totalCount:{},执行时间:{}",
                    TITLE,tcyrSyncFile.getId(),tcyrSyncFile.getFileName(),totalCount,System.currentTimeMillis()-start);
        } catch (IOException e) {
            //修改txt处理异常状态
            tcyrSyncFile.setDealStatus(3);
            tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }


    private void processSingleLineData(Long syncFileId,String apiCode, String batchNo,String line) {
        MarketingTcyrSync syncItem = new MarketingTcyrSync();
        String[] data = line.split(",");
        int dataStatus = 0;
        // length=1: 空字符串/没有逗号 赋值给第一个字段
        // length=2 userKey:column1、terminal:column
        // length>2  多余的数据放入extend:扩展字段(jsonObject)
        if (data.length ==1) {
            syncItem.setUserKey(line);
        }else if (data.length >=2) {
            syncItem.setUserKey(data[0].trim());
            syncItem.setTerminal(data[1].trim());
            dataStatus =1;
        }
        JSONObject extentJson = new JSONObject();
        for (int i = 0; i < data.length; i++) {
            extentJson.put("column_"+(i+1), data[i]);
        }
        syncItem.setExtend(extentJson.toJSONString());
        syncItem.setApiCode(apiCode);
        syncItem.setBatchNo(batchNo);
        syncItem.setCreateTime(new Date());
        syncItem.setStatus(dataStatus);
        syncItem.setSyncFileId(syncFileId);
        tcyrSyncMapper.insertSelective(syncItem);
    }

    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        executor.shutdown();
        try {
            while (!executor.awaitTermination(60L, TimeUnit.SECONDS)) {
                log.info("{},线程池关闭",TITLE);
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            log.error("{},日志保存线程池结束异常！",TITLE,ex);
            Thread.currentThread().interrupt();
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }
}
