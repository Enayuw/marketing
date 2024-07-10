package com.br.marketing.monkey.job.wuba;

import com.br.marketing.service.Impl.wuba.WuBaCollidingDataSubmitService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 58提交撞库数据作业
 *
 * @Author chenh
 * @Date 2024-07-10
 */
@Component
@Slf4j
public class WuBaCollidingDataSubmitJob extends AbstractSimpleElasticJob {
    @Resource
    WuBaCollidingDataSubmitService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        service.process(context);
        log.warn("58提交撞库数据作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
