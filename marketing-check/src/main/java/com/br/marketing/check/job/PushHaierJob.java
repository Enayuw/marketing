package com.br.marketing.check.job;


import com.br.marketing.service.PushDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class PushHaierJob extends AbstractSimpleElasticJob {

    @Autowired
    PushDataService pushDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        pushDataService.pushHaierData();
    }
}
