package com.br.marketing.rule.job;

import com.br.marketing.service.rulecenter.IRuleRefreshConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 数禾定制营销自动推决策-3710128、3710148
 * 需求：https://c.100credit.cn/pages/viewpage.action?pageId=178646155
 */
@Component
@Slf4j
public class AutoRefreshConfigJob extends AbstractSimpleElasticJob {


    @Resource
    IRuleRefreshConfigService iRuleRefreshConfigService;

    private static final String TITLE = "【自动刷新推决策配置】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.info(TITLE + "start");
        long start = System.currentTimeMillis();
        iRuleRefreshConfigService.autoRefreshConfig();
        long end = System.currentTimeMillis();
        log.info(TITLE + "end, 耗时{}ms", end-start);
    }
}
