package com.br.marketing.task.job;

import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.service.Impl.ObservedScoreThreadServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 暂停跑分任务
 */
@Component
@Slf4j
public class TaskStopJob extends AbstractSimpleElasticJob {

    @Autowired
    ObservedScoreThreadServiceImpl observedScoreThreadService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        if(StringUtils.isNotBlank(jobParameter)){
            if(jobParameter.equals("1")){
                TaskExecCommonField.isExecTaskJob=1;
            }else{
                observedScoreThreadService.stopThread();
                observedScoreThreadService.removeThread();
                TaskExecCommonField.isExecTaskJob=Integer.valueOf(jobParameter);;
            }
        }
    }
}
