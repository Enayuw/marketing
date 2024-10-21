package com.br.marketing.rule.job;

import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.rulecenter.IRuleRefreshConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class AutoRefreshConfigJob extends AbstractSimpleElasticJob {


    @Resource
    IRuleRefreshConfigService iRuleRefreshConfigService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        iRuleRefreshConfigService.buildRefreshConfig();
    }
}
