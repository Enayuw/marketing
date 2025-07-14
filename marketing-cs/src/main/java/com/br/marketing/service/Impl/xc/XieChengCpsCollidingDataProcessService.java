package com.br.marketing.service.Impl.xc;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

/**
 * 携程CPS撞库数据同步服务接口
 * @Author chenh
 * @Date 2025-06-26
 */
public interface XieChengCpsCollidingDataProcessService {
    /**
     * 数据同步处理
     * @param context 作业执行上下文
     */
    void process(JobExecutionMultipleShardingContext context);
} 