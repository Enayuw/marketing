package com.br.marketing.xc.job;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import lombok.extern.slf4j.Slf4j;
import com.google.common.collect.Lists;

/**
 * 携程动态包撞库数据补充作业
 *
 * @author senyang.zheng
 * @date 2024/06/19
 */
@Slf4j
@Component
public class XieChengFalseDynamicCollidingDataJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengCollidingDataLogMapper logMapper;
    @Resource
    private XieChengCollidingDataRobMapper robMapper;

    public static final int SIZE = 1000;
    public static final Long PACKAGE_ID = 120007L;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
        List<XieChengCollidingDataLog> logs = logMapper.selectFalseDynamicData();
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        List<List<XieChengCollidingDataLog>> partitions = Lists.partition(logs, SIZE);
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        partitions.forEach((List<XieChengCollidingDataLog> logData) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> saveFalseDynamicData(logData), threadPool);
            futures.add(future);
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("动态包撞库数据补充：线程池关闭");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "动态包撞库数据补充：线程池关闭结束异常！"), e);
            Thread.currentThread().interrupt();
        }
    }

    private void saveFalseDynamicData(List<XieChengCollidingDataLog> logData) {
        List<XieChengCollidingDataRob> robs = Lists.newArrayList();
        for (XieChengCollidingDataLog log : logData) {
            XieChengCollidingDataRob rob = new XieChengCollidingDataRob();
            rob.setPackageId(PACKAGE_ID);
            rob.setDataSourceType("F");
            rob.setCellSha256CodeList(log.getCellSha256CodeList());
            robs.add(rob);
        }
        robMapper.batchSaveFalseDynamicData(robs);
    }
}
