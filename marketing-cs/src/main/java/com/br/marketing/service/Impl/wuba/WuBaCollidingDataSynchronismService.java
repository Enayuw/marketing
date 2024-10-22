package com.br.marketing.service.Impl.wuba;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

import java.util.List;

public interface WuBaCollidingDataSynchronismService {
    void process(JobExecutionMultipleShardingContext context);
    List<Long> getHighValueFileIds(String apiCode);
}
