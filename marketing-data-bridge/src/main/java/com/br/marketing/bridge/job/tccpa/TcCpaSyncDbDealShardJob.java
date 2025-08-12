package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaSuccessDataDbDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同程CPA->撞库db流程->周期表
 */

@Component
@Slf4j
public class TcCpaSyncDbDealShardJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaSuccessDataDbDealService tcCpaSuccessDataDbDealService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaSuccessDataDbDealService.shardProcess(marketingCommonConfig.getTcyrCpaApiCode());
    }
}
