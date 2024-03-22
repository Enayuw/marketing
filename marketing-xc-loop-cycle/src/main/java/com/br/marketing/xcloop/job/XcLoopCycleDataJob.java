package com.br.marketing.xcloop.job;

import com.br.marketing.service.Impl.xc.XcLoopCycleDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 携程TRUE数据作业
 * @Author hong.chen
 * @CreateTime 2024/03/20
 */
@Component
@Slf4j
public class XcLoopCycleDataJob extends AbstractSimpleElasticJob {
    @Resource
    XcLoopCycleDataService service;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        service.process();
    }
}
