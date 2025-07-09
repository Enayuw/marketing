package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengCpsRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 携程CPS非周期撞库作业
 * @Author chenh
 * @Date 2025-06-26
 */
@Component
@Slf4j
public class XieChengCpsRobDataCollidingJob extends AbstractSimpleElasticJob {
    
    @Resource
    private XieChengCpsRobDataCollidingService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        service.process();
        log.warn("携程CPS非周期撞库作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
} 