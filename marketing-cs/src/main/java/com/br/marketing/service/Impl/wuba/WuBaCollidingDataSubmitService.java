package com.br.marketing.service.Impl.wuba;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface WuBaCollidingDataSubmitService {
    void process(JobExecutionMultipleShardingContext context);
}
