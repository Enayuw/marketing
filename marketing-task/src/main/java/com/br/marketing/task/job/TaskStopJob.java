package com.br.marketing.task.job;

import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.CustomerMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 暂停跑分任务
 */
@Component
@Slf4j
public class TaskStopJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        if(StringUtils.isNotBlank(jobParameter)){
            if(jobParameter.equals("0")){
                TaskExecCommonField.isExecTaskJob=0;
            }else{
                TaskExecCommonField.isExecTaskJob=1;
            }
        }
    }
}
