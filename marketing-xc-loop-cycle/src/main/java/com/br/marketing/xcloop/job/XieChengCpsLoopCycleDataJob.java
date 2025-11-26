package com.br.marketing.xcloop.job;

import com.br.marketing.service.Impl.xc.XieChengCpsLoopCycleDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 携程CPS周期撞库数据Job
 * 2025-11-04 产品在携程钉钉群里通知业务暂停，作业置为永久失效
 * @Author chenh
 * @Date 2025-06-26
 */
@Component
@Slf4j
public class XieChengCpsLoopCycleDataJob extends AbstractSimpleElasticJob {
    @Resource
    private XieChengCpsLoopCycleDataService service;

    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        service.process();
        log.warn("携程CPS周期数据撞库作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
} 