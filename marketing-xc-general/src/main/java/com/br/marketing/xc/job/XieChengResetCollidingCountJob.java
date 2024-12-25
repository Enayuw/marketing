package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 携程重置撞库次数，并生成非周期撞库任务
 *
 * @author hong.chen
 * @date 2024/12/06
 */
@Slf4j
@Component
public class XieChengResetCollidingCountJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengRobDataCollidingService xieChengRobDataCollidingService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        xieChengRobDataCollidingService.resetCollidingCountAndBuildRobTask();
        log.warn("携程重置撞库次数任务，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
