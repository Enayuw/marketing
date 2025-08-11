package com.br.marketing.bridge.job;

import com.br.marketing.service.TransFileToMarketingBiShardService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author xiong.luo
 * @description: 内部服务器的转化文件落库到marketingBI(分片)
 * @date 2025/06/30
 */
@Component
@Slf4j
public class TransFileToMarketingBIShardJob extends AbstractSimpleElasticJob {

    @Resource
    private TransFileToMarketingBiShardService  transFileToMarketingBiShardService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long startTime = System.currentTimeMillis();
        log.warn("内部服务器转化文件提取到marketingBI分片任务开始...");
        transFileToMarketingBiShardService.process(shardingContext.getJobParameter(), shardingContext.getShardingItems());
        log.warn("内部服务器转化文件提取到marketingBI分片任务结束,耗时:{}", System.currentTimeMillis() - startTime);
    }
}
