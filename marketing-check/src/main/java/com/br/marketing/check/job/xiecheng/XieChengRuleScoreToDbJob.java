package com.br.marketing.check.job.xiecheng;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description XieChengRuleScoreToDbJob
 * @Author hong.chen
 * @CreateTime 2024/04/22
 */
@Component
@Slf4j
public class XieChengRuleScoreToDbJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }
}
