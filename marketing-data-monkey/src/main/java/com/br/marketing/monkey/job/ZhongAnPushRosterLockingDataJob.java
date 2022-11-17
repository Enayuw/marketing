package com.br.marketing.monkey.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

/**
 * 名单锁定推送众安
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/17 17:49
 */
public class ZhongAnPushRosterLockingDataJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }
}
