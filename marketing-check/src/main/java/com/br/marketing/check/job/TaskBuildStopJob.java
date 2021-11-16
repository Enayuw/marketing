package com.br.marketing.check.job;

import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.utils.StringUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 暂停生成跑分任务
 */
@Component
@Slf4j
public class TaskBuildStopJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        if(StringUtils.isNotBlank(jobParameter)){
            if(jobParameter.equals("1")){
                TaskExecCommonField.isBuildTaskJob=1;
            }else{
                TaskExecCommonField.isBuildTaskJob=Integer.valueOf(jobParameter);
            }
        }
    }
}
