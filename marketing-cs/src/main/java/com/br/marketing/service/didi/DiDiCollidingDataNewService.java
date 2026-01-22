package com.br.marketing.service.didi;

import com.br.marketing.common.commondto.Result;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface DiDiCollidingDataNewService {
    void colliding(JobExecutionMultipleShardingContext context);
}
