package com.br.marketing.monkey.job.yunke;

import com.br.marketing.service.YunKeService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author peng.kang
 * @description: 获取云客机型
 * @date 2025/5/25 18:53
 */
@Component
@Slf4j
public class CollectDeviceTypeJob extends AbstractSimpleElasticJob {
    @Resource
    YunKeService yunKeService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        log.warn("云客机型获取job任务开始");
        yunKeService.getDeviceType();
        log.warn("云客机型获取job任务结束, 耗时:{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
