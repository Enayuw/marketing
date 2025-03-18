package com.br.marketing.check.job.mark;

import com.br.marketing.service.mark.DataBlackListMarkService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @author peng.kang
 * @description: pp停车-与外呼黑名单打标
 * @date 2025/2/21 10:12
 */
@Component
@Slf4j
public class DataBlackListMarkJob extends AbstractSimpleElasticJob {
    @Resource
    DataBlackListMarkService dataBlackListMarkService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        dataBlackListMarkService.process();
        log.warn("pp停车-与黑名单求交打标, 运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
