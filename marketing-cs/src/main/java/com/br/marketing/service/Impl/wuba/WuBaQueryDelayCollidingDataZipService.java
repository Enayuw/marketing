package com.br.marketing.service.Impl.wuba;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface WuBaQueryDelayCollidingDataZipService {
    void process(JobExecutionMultipleShardingContext context);
}
