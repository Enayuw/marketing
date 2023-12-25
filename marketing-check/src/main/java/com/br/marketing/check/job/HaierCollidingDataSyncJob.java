package com.br.marketing.check.job;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.mapper.HaierCollidingDataLogMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HaierCollidingDataSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private HaierCollidingDataLogMapper haierCollidingDataLogMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }
}
