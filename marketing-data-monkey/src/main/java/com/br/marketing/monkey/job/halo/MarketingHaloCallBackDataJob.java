package com.br.marketing.monkey.job.halo;

import com.br.marketing.service.MarketingHaloCallBackDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;


/**
 * @Description 哈啰-三方营销数据回传 job
 * @Author zhiyong.zhang
 * @CreateTime 2025/10/16
 * 需求:https://c.100credit.cn/pages/viewpage.action?pageId=223429538
 */
@Component
@Slf4j
public class MarketingHaloCallBackDataJob extends AbstractSimpleElasticJob {


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingHaloCallBackDataService marketingHaloCallBackDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        marketingHaloCallBackDataService.process(marketingCommonConfig.getHaloCallBackDataConfig().getString("apiCode"));
    }
}
