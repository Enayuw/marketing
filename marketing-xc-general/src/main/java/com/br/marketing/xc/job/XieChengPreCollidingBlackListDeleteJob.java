package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengPreCollidingBlackListDeleteService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
* @Description:携程撞库黑名单剔除
* @Author: Ethan.Kang
*/
@Component
@Slf4j
public class XieChengPreCollidingBlackListDeleteJob extends AbstractSimpleElasticJob {

    @Resource
    XieChengPreCollidingBlackListDeleteService service;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext){
        service.process();
    }

}
