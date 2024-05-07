package com.br.marketing.check.job.xiecheng;

import com.br.marketing.service.Impl.xc.XieChengRuleScoreToDbService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description 携程跑分数据同步作业
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=155705775
 * @Author hong.chen
 * @CreateTime 2024/04/22
 */
@Component
@Slf4j
public class XieChengCollidingRuleScoreToDbJob extends AbstractSimpleElasticJob {
    @Autowired
    XieChengRuleScoreToDbService service;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        service.process(jobExecutionMultipleShardingContext);
        log.warn("携程跑分数据同步作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
