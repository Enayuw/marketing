package com.br.marketing.check.job;

import com.br.marketing.check.service.ShuHeTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author lizhen
 * @Date 2022/5/28 20:46
 * @Description:数禾黑名单推电销转化接口
 **/
@Component
@Slf4j
public class ShuHeBlackPhonePushDaasJob extends AbstractSimpleElasticJob {

    @Autowired
    private ShuHeTransferService shuHeTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        shuHeTransferService.pushBlackDataToDaas();

    }
}
