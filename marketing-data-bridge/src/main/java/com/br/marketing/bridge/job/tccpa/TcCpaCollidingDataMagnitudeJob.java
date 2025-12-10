package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaCollidingDataMagnitudeService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同程CPA撞库数据同步任务
 */
@Component
@Slf4j
public class TcCpaCollidingDataMagnitudeJob extends AbstractSimpleElasticJob {

    @Resource
    private TcCpaCollidingDataMagnitudeService tcCpaCollidingDataMagnitudeService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaCollidingDataMagnitudeService.process();
    }
}
