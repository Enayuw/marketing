package com.br.marketing.xc.job;

import com.br.marketing.service.Impl.xc.XieChengCollidingDataProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 携程动态补充包数据剔除
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=186196574
 * @Author hedongshuo
 * @CreateTime 2024/11/08
 */
@Component
@Slf4j
public class XieChengDynaRobDeleteDataJob extends AbstractSimpleElasticJob {
    @Resource
    XieChengCollidingDataProcessService service;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        service.processDynaDelete();
        log.warn("携程动态补充包数据剔除作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
