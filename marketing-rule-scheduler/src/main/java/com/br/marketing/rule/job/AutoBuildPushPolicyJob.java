package com.br.marketing.rule.job;


import com.br.marketing.service.rulecenter.IRuleCenterEntranceService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class AutoBuildPushPolicyJob extends AbstractSimpleElasticJob {


    @Resource
    IRuleCenterEntranceService iRuleCenterService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        iRuleCenterService.buildPolicyTask();
    }
}
