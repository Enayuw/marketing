package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengBlackEncAndDecJobService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description:携程撞库黑名单log解密-sha256加密
 * @Author: Ethan.Kang
 */
@Component
@Slf4j
public class XieChengBlackEncAndDecJob extends AbstractSimpleElasticJob {
    @Resource
    XieChengBlackEncAndDecJobService service;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long curTime = System.currentTimeMillis();
        log.warn("携程处理撞库黑名单cell log解密转sha256加密任务开始");
        service.process();
        log.warn("携程处理撞库黑名单cell log解密转sha256加密完成, 耗时:{}s", (System.currentTimeMillis() - curTime) / 1000);
    }
}
