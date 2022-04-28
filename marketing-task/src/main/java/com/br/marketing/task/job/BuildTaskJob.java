package com.br.marketing.task.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.stereotype.Component;

@Component
public class BuildTaskJob  extends AbstractSimpleElasticJob {


    /**
     * 生成自动任务
     * 1、获取有效跑分并且规则类型condition_type=1规则
     * 2、判断当前规则的时间是否符合
     * 3、判断当前是否有符合的数据
     *
     * @param jobExecutionMultipleShardingContext
     */
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        //region

        //endregion
    }
}
