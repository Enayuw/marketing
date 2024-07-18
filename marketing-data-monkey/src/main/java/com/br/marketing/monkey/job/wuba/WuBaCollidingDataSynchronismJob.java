package com.br.marketing.monkey.job.wuba;

import com.br.marketing.service.Impl.wuba.WuBaCollidingDataSynchronismService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 58撞库数据同步作业
 * @Author chenh
 * @Date 2024-07-10
 */
@Component
@Slf4j
public class WuBaCollidingDataSynchronismJob extends AbstractSimpleElasticJob {
    @Resource
    WuBaCollidingDataSynchronismService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        service.process(context);
        log.warn("58撞库数据同步作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
