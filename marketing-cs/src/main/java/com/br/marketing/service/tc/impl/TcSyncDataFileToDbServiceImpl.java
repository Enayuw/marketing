package com.br.marketing.service.tc.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataFileToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
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

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrSyncTxtToDb.concat(apiCode);;
        String lockValue = "";
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_FILE_TO_DB.getName(),100,100);
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
                tcyrSyncFile.setUpdateTime(new Date());
                tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
                redisChgService.unlock(lockKey, lockValue);
                //4、处理txt数据入库
                parseCsvFileToDb(tcyrSyncFile,actionPool);
            }
            Thread.sleep(marketingCommonConfig.getTcTxtFileShardConfig().getInteger("dbCountWaitTime"));
            //4、计算dbCount
            List<MarketingTcyrSyncFile> syncFileList = tcyrSyncFileMapper.selectSyncFileList(apiCode,2);
            syncFileList.forEach(tcyrSyncFile -> {
                Long dbCount = tcyrSyncMapper.selecFileDbCount(tcyrSyncFile.getApiCode(),tcyrSyncFile.getId());
                tcyrSyncFile.setSuccessCount(dbCount);
                tcyrSyncFile.setUpdateTime(new Date());
                tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
                if (!Objects.equals(tcyrSyncFile.getTotalCount(), dbCount)) {
                    String alertMsg =String.format("文件总数和db数量不一致,fileId:%s,fileName:%s,totalCount:%s,dbCount:%s",
                            tcyrSyncFile.getId(),tcyrSyncFile.getFileName(),tcyrSyncFile.getTotalCount(),dbCount);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                            alertMsg, TITLE));
                }
            });
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            //5、异常时释放锁(finally)
            redisChgService.unlock(lockKey, lockValue);
            actionPool.shutdownAndAwaitTermination();
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
        MarketingTcyrSyncRecord syncRecord = tcyrSyncRecordMapper.selectByPrimaryKey(tcyrSyncFile.getSyncRecordId());
        // 2、txt文件解析入库
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            Long totalCount = 0L;
            String line;
            while ((line = reader.readLine()) != null) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(actionPool, marketingCommonConfig.getTcTxtFileShardConfig().getInteger("threadPool"));
                String finalLine = line;
                CompletableFuture.runAsync(() -> processSingleLineData(tcyrSyncFile.getId(),
                            tcyrSyncFile.getApiCode(), tcyrSyncFile.getBatchNo(), finalLine,syncRecord.getData()), actionPool);
                totalCount ++;
            }
            //3、修改txt完成状态、总成功条数
            tcyrSyncFile.setDealStatus(2);
            tcyrSyncFile.setTotalCount(totalCount);
            tcyrSyncFile.setUpdateTime(new Date());
            tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
            log.warn("TITLE:{} csv文件入db完成,syncFileId:{},csvName:{},totalCount:{},执行时间:{}",
                    TITLE,tcyrSyncFile.getId(),tcyrSyncFile.getFileName(),totalCount,System.currentTimeMillis()-start);
        } catch (IOException e) {
            //修改txt处理异常状态
            tcyrSyncFile.setDealStatus(3);
            tcyrSyncFile.setUpdateTime(new Date());
            tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }


    private void processSingleLineData(Long syncFileId,String apiCode, String batchNo,String line,String customerData) {
        MarketingTcyrSync syncItem = new MarketingTcyrSync();
        String[] data = line.split(",");
        int dataStatus = 0;
        // length=1: 空字符串/一个字符串没有逗号 赋值给第一个字段
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
        JSONObject customJson = JSONObject.parseObject(customerData);
        List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrSyncSaveExcludeFieldList();
        for (String key : customJson.keySet()) {
            if (!tcyrSyncExcludeFieldList.contains(key)) {
                extentJson.put(key, customJson.get(key));
            }
        }
        syncItem.setExtend(extentJson.toJSONString());
        syncItem.setApiCode(apiCode);
        syncItem.setBatchNo(batchNo);
        syncItem.setCreateTime(new Date());
        syncItem.setStatus(dataStatus);
        syncItem.setSyncFileId(syncFileId);
        tcyrSyncMapper.insertSelective(syncItem);
    }
}
