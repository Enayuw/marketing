package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengCollidingDataProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 携程撞库数据处理作业
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=155705775
 * @Author hong.chen
 * @CreateTime 2024/04/24
 */
@Component
@Slf4j
public class XieChengCollidingDataProcessJob extends AbstractSimpleElasticJob {
    @Resource
    XieChengCollidingDataProcessService service;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        service.process();
        log.warn("携程撞库数据处理作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
