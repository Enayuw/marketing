package com.br.marketing.check.job.juzi;

import com.br.marketing.check.service.JuZiPushDassService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 桔子周期性自动化转Daas-3710037（营销→Daas）
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/19 10:18
 */
@Component
@Slf4j
public class JuZiTransferCyclicalPushDaasJob extends AbstractSimpleElasticJob {

    @Resource
    private JuZiPushDassService juZiPushDassService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {

    }
}
