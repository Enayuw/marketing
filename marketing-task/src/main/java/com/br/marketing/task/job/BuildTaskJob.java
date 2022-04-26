package com.br.marketing.task.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.stereotype.Component;

@Component
public class BuildTaskJob  extends AbstractSimpleElasticJob {


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }
}
