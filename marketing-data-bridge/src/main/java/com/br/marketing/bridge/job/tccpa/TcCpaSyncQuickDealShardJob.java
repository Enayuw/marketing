package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaSyncDataQuickDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 同程易融CPA快速处理流程(file->上传明细表)
 * @Author zhiyong.zhang
 * @CreateTime 2025/07/03
 */
@Component
@Slf4j
public class TcCpaSyncQuickDealShardJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Resource
    private TcCpaSyncDataQuickDealService tcCqaSyncDataQuickDealService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCqaSyncDataQuickDealService.shardProcess(marketingCommonConfig.getTcyrCpaApiCode());
    }
}
