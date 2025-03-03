package com.br.marketing.check.job.qifu;

import com.br.marketing.service.Impl.qifu.IQiFuService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 奇富ai清洗
 */
@Component
@Slf4j
public class QiFuAiCleanJob extends AbstractSimpleElasticJob {
    @Resource
    private IQiFuService iQiFuService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("奇富ai清洗开始");
        long start = System.currentTimeMillis();
        iQiFuService.aiCleanProcess();
        long end = System.currentTimeMillis();
        log.warn("奇富ai清洗耗时："+(end-start));
    }
}
