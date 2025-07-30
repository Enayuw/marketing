package com.br.marketing.service.Impl.zhongan;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface ZhongAnCollidingDataService {
    void process(JobExecutionMultipleShardingContext context);
}
