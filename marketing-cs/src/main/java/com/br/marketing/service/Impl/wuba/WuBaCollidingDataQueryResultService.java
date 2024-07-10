package com.br.marketing.service.Impl.wuba;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface WuBaCollidingDataQueryResultService {
    void process(JobExecutionMultipleShardingContext context);
}
