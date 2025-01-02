package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingManyTimesService;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 携程非周期撞库
 *
 * @author senyang.zheng
 * @date 2024/04/18
 */
@Slf4j
@Component
public class XieChengRobDataCollidingManyTimesJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengRobDataCollidingManyTimesService xieChengRobDataCollidingManyTimesService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        xieChengRobDataCollidingManyTimesService.collidingDataManyTimes();
    }

}
