package com.br.marketing.task.job;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.task.service.ITaskService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BuildTaskJob extends AbstractSimpleElasticJob {

    @Autowired
    ITaskService iTaskService;

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
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        List<Long> ids = null;
        if (StringUtils.isNotBlank(jobParameter)) {
            ids = Arrays.stream(jobParameter.split(",")).map(t -> Long.valueOf(t)).collect(Collectors.toList());
        }
        iTaskService.buildScoreTask(ids, jobExecutionMultipleShardingContext.getJobName());
    }
}
