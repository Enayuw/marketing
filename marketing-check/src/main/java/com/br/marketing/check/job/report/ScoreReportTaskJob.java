package com.br.marketing.check.job.report;

import com.br.marketing.check.service.Impl.scorereport.ScoreReportTaskService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
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

    @Resource
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        ReportTask reportTask = getScoreReportTask();
        if (Objects.isNull(reportTask)) {
            return;
        }
        scoreReportTaskService.scoreReportCount(reportTask);
        //更新报表任务
        ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
        statisticsScoreExample.createCriteria()
                .andReportIdEqualTo(reportTask.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);
        Long failNum = statisticsScoreList.stream().filter(reportStatisticsScore -> reportStatisticsScore.getStatus() != 1).count();
        reportTask.setStatus(failNum > 0 ? 3 : 2);
        reportTask.setUpdateTime(new Date());
        reportTaskMapper.updateByPrimaryKey(reportTask);

    }

    private ReportTask getScoreReportTask() {
        ReportTaskExample taskExample = new ReportTaskExample();
        taskExample.createCriteria()
                .andStatusEqualTo(0)
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
                    if (!("0").equals(task.getStatus())) {
                        redisChgService.unlock(redisKey, s);
                        continue;
                    }
                    ReportTask update = new ReportTask();
                    update.setStatus(1);
                    update.setUpdateTime(new Date());
                    update.setId(reportTask.getId());
                    reportTaskMapper.updateByPrimaryKey(update);
                    return reportTask;
                }
            } catch (Exception e) {
                log.error("跑分模型报表获取任务失败");
            } finally {
                redisChgService.unlock(redisKey, s);
            }
        }

        return null;
    }
}
