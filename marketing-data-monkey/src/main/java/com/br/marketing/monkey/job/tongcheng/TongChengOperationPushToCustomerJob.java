package com.br.marketing.monkey.job.tongcheng;

import com.br.marketing.service.Impl.tongcheng.TongChengOperationPushToCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同程集团迁移可营销名单JOB
 *
 * @author guangxiu.li
 * @dateTime 2024/01/25 16:13
 */
@Component
@Slf4j
public class TongChengOperationPushToCustomerJob extends AbstractSimpleElasticJob {


    @Autowired
    TongChengOperationPushToCustomerService service;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        log.warn("同程集团迁移可营销名单JOB调度开始");
        marketingCommonConfig.getTongChengGroupOperationApiCodes().forEach((String apiCode) -> {
            try {
                Long st1 = System.currentTimeMillis();
                service.process(apiCode);
                log.warn("同程集团运营名单推送客户JOB，耗时：{} ms", System.currentTimeMillis() - st1);
            } catch (Exception e) {
                log.error("同程集团运营名单推送客户JOB异常", e);
            }

        });
    }
}
