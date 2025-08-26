package com.br.marketing.check.job.report;

import com.br.common.log.AlertLog;
import com.br.marketing.check.service.Impl.scorereport.ScoreReportTaskService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.enums.report.ReportTaskStatusEnum;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.ReportTaskMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 跑分报表任务统计
 *
 * @author zhen.Li1
 * @dateTime 2024/08/15 20:07
 */
@Component
@Slf4j
public class ScoreReportTaskJob extends AbstractSimpleElasticJob {

    @Autowired
    private ReportTaskMapper reportTaskMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private ScoreReportTaskService scoreReportTaskService;



    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("【跑分报表任务统计】开始");
        long start = System.currentTimeMillis();
        ReportTask reportTask = getScoreReportTask();
        if (Objects.isNull(reportTask)) {
            return;
        }
        scoreReportTaskService.scoreReportCount(reportTask);
        log.warn("【跑分报表任务统计】结束耗时{} ms", System.currentTimeMillis() - start);
    }

    private ReportTask getScoreReportTask() {
        ReportTaskExample taskExample = new ReportTaskExample();
        taskExample.createCriteria()
                .andStatusEqualTo(ReportTaskStatusEnum.READY.getValue())
                .andReportTypeEqualTo(ReportTaskTypeEnum.SCORE_MODEL_TYPE.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        taskExample.setOrderByClause(" create_time");
        List<ReportTask> reportTaskList = reportTaskMapper.selectByExample(taskExample);
        for (ReportTask reportTask : reportTaskList) {
            String redisKey = RedisKeyConstant.SCORE_REPORT_TASK_LOCK.concat(reportTask.getId().toString());
            String s = UUID.randomUUID().toString();
            try {
                boolean lock = redisChgService.lock(redisKey, s,
                        5000L);
                if (lock) {
                    ReportTask task = reportTaskMapper.selectByPrimaryKey(reportTask.getId());
                    if (!ReportTaskStatusEnum.READY.getValue().equals(task.getStatus())) {
                        redisChgService.unlock(redisKey, s);
                        continue;
                    }
                    ReportTask update = new ReportTask();
                    update.setStatus(ReportTaskStatusEnum.RUNNING.getValue());
                    update.setUpdateTime(new Date());
                    update.setId(reportTask.getId());
                    reportTaskMapper.updateByPrimaryKeySelective(update);
                    return reportTask;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "跑分模型报表统计获取任务异常"), e);
            } finally {
                redisChgService.unlock(redisKey, s);
            }
        }
        return null;
    }
}
