package com.br.marketing.service.didi;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface DiDiCollidingDataService {
    void colliding(JobExecutionMultipleShardingContext context);
}
