package com.br.marketing.check.job;


import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class HaluoDassJob extends AbstractSimpleElasticJob {

    @Autowired
    PhoneSaleExtendServiceImpl phoneSaleExtendService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        phoneSaleExtendService.haluoPushDass();
    }
}
