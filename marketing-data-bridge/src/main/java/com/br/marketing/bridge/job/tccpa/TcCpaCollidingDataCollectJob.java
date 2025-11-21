package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaCollidingDataCollectService;
import com.br.marketing.service.tccpa.TcCpaCollidingDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同程CPA撞库数据同步任务
 */
@Component
@Slf4j
public class TcCpaCollidingDataCollectJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaCollidingDataCollectService tcCpaCollidingDataCollectService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaCollidingDataCollectService.process(marketingCommonConfig.getTcyrCpaApiCode());
    }
}
