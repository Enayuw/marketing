package com.br.marketing.task.job;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.task.service.Impl.TaskScoreServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Component
@Slf4j
public class TaskScoreStartJob extends AbstractSimpleElasticJob {
    @Resource
    CustomerMapper customerMapper;

    @Autowired
    ITaskService iTaskService;

    @Autowired
    TaskScoreServiceImpl taskScoreService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        Long start=System.currentTimeMillis();
        log.warn("【跑批任务】调度开始");
        String jobParameter = context.getJobParameter();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Long taskId = 0L;
        if(StringUtils.isNotBlank(jobParameter)){
            String[] split = jobParameter.split(",");
            for (int i = 0; i < split.length; i++) {
                if(i==0){
                    date = split[i];
                }else{
                    taskId = Long.valueOf(split[1]);
                }
            }
        }
        Result<MarketingTask> scoreTask = iTaskService.getScoreTask(date,taskId);
        if(ResultCode.SUCCESS.getValue().equals(scoreTask.getCode())){
            MarketingTask marketingTask = scoreTask.getData();
            taskScoreService.process(marketingTask,date);
        }
        Long end =System.currentTimeMillis();
        log.warn("【跑批任务】调度结束，耗时：{},分片：{}",end-start,context.getShardingItemParameters());
    }
}
