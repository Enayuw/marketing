package com.br.marketing.check.job.mark;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author peng.kang
 * @description: pp停车-与外呼黑名单打标
 * @date 2025/2/21 10:12
 */
@Component
@Slf4j
public class DataBlackListMarkJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }
}
