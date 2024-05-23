package com.br.marketing.service.Impl.xc;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface XieChengRuleScoreToDbService {
    void process(JobExecutionMultipleShardingContext context);
}
