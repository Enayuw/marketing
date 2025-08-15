package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaCollidingFailDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * 同程cpa撞库失败数据处理
 */
@Component
@Slf4j
public class TcCpaCollidingFailDealShardJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaCollidingFailDealService tcCpaCollidingFailDealService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaCollidingFailDealService.process(marketingCommonConfig.getTcyrCpaApiCode());
    }
}
