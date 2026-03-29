package com.br.marketing.monkey.job.taikang;


import com.br.marketing.service.MarketingTaikangDingDingTransferService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Description 泰康大健康线索钉钉数据回传
 * @Author zhiyong.zhang
 * @CreateTime 2026/01/23
 * 需求:https://c.100credit.cn/pages/viewpage.action?pageId=236766830
 */
@Component
@Slf4j
public class TaikangDingDingTransferJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTaikangDingDingTransferService taikangDDTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
        String apiCodes = taikangConfig.getOrDefault("apiCode", "3750004");
        taikangDDTransferService.process(apiCodes);
    }
}
