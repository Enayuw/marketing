package com.br.marketing.task.job;

import com.br.marketing.task.service.ITaskService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 跑分任务自动插队JOB
 */
@Component
@Slf4j
public class TaskScoreJumpQueueJob extends AbstractSimpleElasticJob {

    @Autowired
    ITaskService iTaskService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        iTaskService.JumpQueuehandle();
    }
}
