package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.IPeriodPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Map;

/**
 * job设计目的是等数据积攒到一个时间周期后集中推送决策
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=160813688
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-28
 */
@Slf4j
public class PeriodPushJob extends AbstractSimpleElasticJob {

    @Resource
    IPeriodPushService periodPushService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        periodPushService.handle();
    }
}
