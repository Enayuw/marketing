package com.br.marketing.check.job;

import com.br.marketing.check.service.RongShuIbuCycleService;
import com.br.marketing.check.service.RongShuPushDaasService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.beans.factory.annotation.Autowired;

public class RongShuCyclePushDaasJob extends AbstractSimpleElasticJob {


    @Autowired
    private RongShuIbuCycleService rongShuIbuCycleService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        rongShuIbuCycleService.pushCycleDataToDaas();

    }
}
