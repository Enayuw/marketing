package com.br.marketing.bridge.job;


import com.br.marketing.service.LineBaseInfoSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 线路侧三方数据同步
 */
@Component
@Slf4j
public class LineBaseInfoSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private LineBaseInfoSyncService lineBaseInfoSyncService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        lineBaseInfoSyncService.process();
    }
}
