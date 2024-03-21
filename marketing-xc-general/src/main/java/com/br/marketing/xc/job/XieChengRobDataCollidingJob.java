package com.br.marketing.xc.job;

import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class XieChengRobDataCollidingJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengRobDataCollidingService xieChengRobDataCollidingService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // TODO 增加强制开关和条件开关
        Boolean strongSwitch = new Random().nextInt(10) % 2 == 0 ? Boolean.FALSE : Boolean.TRUE;
        Boolean conditionSwitch = new Random().nextInt(10) % 2 == 0 ? Boolean.FALSE : Boolean.TRUE;
        // 强制开关开启强制撞库，强制开关关闭且条件开关关闭开始撞库
        if (strongSwitch || conditionSwitch) {
            xieChengRobDataCollidingService.collidingData();
        } else {
            log.info("不进行撞库");
        }
    }
}
