package com.br.marketing.monkey.job.wuba;

import com.br.marketing.service.Impl.tongcheng.TongChengOperationPushToCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 58新客提交营销名单
 *
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaSubmitConversionJob extends AbstractSimpleElasticJob {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    TongChengOperationPushToCustomerService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

    }
}
