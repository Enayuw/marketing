package com.br.marketing.check.service.Impl.qifu;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

public interface QiFuDataCleanService {
    void cleanUploadData(String context);

    void cleanESData(String context);
}
