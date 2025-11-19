package com.br.marketing.check.job.qifu;

import com.br.marketing.check.service.qifu.QiFuAiCleanService;
import com.br.marketing.check.service.qifu.QiFuAiEventPushService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName QiFuAiEventPushCleanJob
 * @Author hang.zhou
 * @Date 2025/11/18
 */
@Component
public class QiFuAiEventPushCleanJob extends AbstractSimpleElasticJob {

    private static final Logger logger = LoggerFactory.getLogger(QiFuAiEventPushCleanJob.class);

    @Resource
    private QiFuAiCleanService qiFuAiCleanService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        logger.warn("奇富ai事件推送实时数据清洗开始");
        long start = System.currentTimeMillis();
        qiFuAiCleanService.aiRealTimeCleanProcessFromOriginal();
        long end = System.currentTimeMillis();
        logger.warn("奇富ai事件推送实时数据清洗耗时：{}", (end - start));
    }
}
