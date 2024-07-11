package com.br.marketing.service.Impl.wuba;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface WuBaCollidingDataSynchronismService {
    void process(JobExecutionMultipleShardingContext context);
}
