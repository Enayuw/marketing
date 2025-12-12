package com.br.marketing.bridge.job;


import com.br.marketing.service.SmsBaseInfoSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 短信侧侧三方数据同步
 */
@Component
@Slf4j
public class SmsBaseInfoSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private SmsBaseInfoSyncService smsBaseInfoSyncService;



    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        smsBaseInfoSyncService.process();
    }
}
