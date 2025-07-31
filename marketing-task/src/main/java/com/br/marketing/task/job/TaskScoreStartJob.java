package com.br.marketing.task.job;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.task.service.Impl.ObservedScoreThreadServiceImpl;
import com.br.marketing.task.service.Impl.TaskScoreServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Autowired
    ObservedScoreThreadServiceImpl observedScoreThreadService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        if (observedScoreThreadService.isInterrupt()) {
            StringBuilder content = new StringBuilder();
            content.append("当前跑分任务手动停止状态请手动开启");
            alarmClient.sendAlarm(content.toString(), "跑分暂停", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
            return;
        }

        Long start = System.currentTimeMillis();
        log.warn("【跑批任务】调度开始");
        String jobParameter = context.getJobParameter();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Long taskId = 0L;
        Integer isTimeLimit = null;
        if (StringUtils.isNotBlank(jobParameter)) {
            isTimeLimit = 1;
            String[] split = jobParameter.split(",");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    date = split[i];
                } else {
                    taskId = Long.valueOf(split[1]);
                }
            }
        }
        Result<MarketingTask> scoreTask = iTaskService.getScoreTask(date, taskId, isTimeLimit, context.getJobName());
        log.warn("跑分任务开始，本次调度任务id：{}", JSON.toJSONString(scoreTask));
        if (ResultCode.SUCCESS.getValue().equals(scoreTask.getCode())) {
            MarketingTask marketingTask = scoreTask.getData();
            marketingTask.setIndex(context.getShardingItems().get(0));
            taskScoreService.process(marketingTask, date);
            log.warn("跑分任务结束，本次调度任务batchNumber：{}", marketingTask.getBatchNumber());
        }
        Long end = System.currentTimeMillis();
        log.warn("【跑批任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }
}
