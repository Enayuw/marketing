package com.br.marketing.service.Impl.wuba;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

/**
 * @description 58新客撞库日志清洗及数据提取接口
 * @author hedongshuo
 * @date 2024/10/18 13:58
 **/
public interface WuBaCleanAndExtractToPackageService {
    void process(JobExecutionMultipleShardingContext context);
}
