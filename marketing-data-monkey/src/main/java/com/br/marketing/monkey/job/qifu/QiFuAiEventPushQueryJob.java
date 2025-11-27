package com.br.marketing.monkey.job.qifu;


import com.br.marketing.monkey.service.qifu.QiFuAiEventPushService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName QiFuAiEventPushCleanJob
 * @Author hang.zhou
 * @Date 2025/11/17
 */
@Component
public class QiFuAiEventPushQueryJob extends AbstractSimpleElasticJob {

    private static final Logger logger = LoggerFactory.getLogger(QiFuAiEventPushQueryJob.class);

    @Resource
    private QiFuAiEventPushService qiFuAiEventPushService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        logger.warn("奇富ai事件推送实时数据查询开始");
        long start = System.currentTimeMillis();
        qiFuAiEventPushService.assembleRealTimeUploadDataOriginal();
        long end = System.currentTimeMillis();
        logger.warn("奇富ai事件推送实时数据查询耗时：{}", (end - start));
    }
}

