package com.br.marketing.monkey.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author lizhen
 * @Description 众安信贷自动化推送黑名单(营销→外呼)
 * @Date 2022/11/15 10:02
 */
@Component
@Slf4j
public class ZhongAnPushBlackDataJob extends AbstractSimpleElasticJob {

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {




    }
}
