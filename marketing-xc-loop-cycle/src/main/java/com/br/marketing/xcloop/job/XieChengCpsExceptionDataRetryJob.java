package com.br.marketing.xcloop.job;

import com.br.marketing.service.Impl.xc.XieChengCpsExceptionDataRetryService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 携程CPS异常重试作业
 * @Author chenh
 * @Date 2025-06-26
 */
@Component
@Slf4j
public class XieChengCpsExceptionDataRetryJob extends AbstractSimpleElasticJob {
    @Resource
    XieChengCpsExceptionDataRetryService service;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        service.process();
        log.warn("携程CPS异常重试作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}