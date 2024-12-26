package com.br.marketing.monkey.job.wuba;

import com.br.marketing.service.Impl.wuba.WuBaCollidingDataQueryResultService;
import com.br.marketing.service.Impl.wuba.WuBaOldCollidingDataQueryResultService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 58查询撞库结果作业
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=193954645
 * @Author chenh
 * @Date 2024-12-26
 */
@Component
@Slf4j
public class WuBaOldCollidingDataQueryResultJob extends AbstractSimpleElasticJob {
    @Resource
    WuBaOldCollidingDataQueryResultService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        service.process(context);
        log.warn("58老客查询撞库结果作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
