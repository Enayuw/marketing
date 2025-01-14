package com.br.marketing.service.Impl.wuba;

import com.br.marketing.common.commondto.Result;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface WuBaCollidingDataQueryResultService {
    void process(JobExecutionMultipleShardingContext context);

    Result<Boolean> buildEliminateAndPushToRobot(String batchIdStr);
}
