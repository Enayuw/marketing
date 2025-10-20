package com.br.marketing.rule.job;

import com.br.marketing.service.rulecenter.IRuleCenterCustomEsService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName EsCustomQueryJob
 * @Author hang.zhou
 * @Date 2025/10/15
 */
@Component
@Slf4j
public class EsCustomQueryJob  extends AbstractSimpleElasticJob {

    @Resource
    private IRuleCenterCustomEsService ruleCenterCustomEsService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        String jobParameter = shardingContext.getJobParameter();
        ruleCenterCustomEsService.queryEsData(Long.valueOf(jobParameter));
    }
}
