package com.br.marketing.xcloop.job;

import com.br.marketing.service.Impl.xc.XcLoopCycleDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 携程TRUE数据作业
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=151477608
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
        long start = System.currentTimeMillis();
        service.process();
        log.warn("携程TRUE数据撞库作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
