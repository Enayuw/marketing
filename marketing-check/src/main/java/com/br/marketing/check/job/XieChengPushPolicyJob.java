package com.br.marketing.check.job;

import com.br.marketing.check.service.XieChengTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author lizhen
 * @Date 2023/03/08 15:46
 * @Description: 携程自动化推送决策
 **/
@Component
@Slf4j
public class XieChengPushPolicyJob extends AbstractSimpleElasticJob {

    @Autowired
    private XieChengTransferService xieChengTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        xieChengTransferService.pushDataToPolicy();

    }
}
