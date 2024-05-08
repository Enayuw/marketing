package com.br.marketing.xc.job;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import lombok.extern.slf4j.Slf4j;

/**
 * 携程重置撞库次数任务
 *
 * @author senyang.zheng
 * @date 2024/05/08
 */
@Slf4j
@Component
public class XieChengResetCollidingCountJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengRobDataCollidingService xieChengRobDataCollidingService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        xieChengRobDataCollidingService.resetCollidingCount();
    }
}
