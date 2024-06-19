package com.br.marketing.task.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTask;

import java.util.List;

public interface ITaskService {

    void buildScoreTask(List<Long> scoreRuleIds,String jobNm);

    /**
     * 获取跑分任务
     * @param date 时间
     * @param taskId 任务id
     * @param isTimeLimit 是否筛选运行时间（HH:mm）小于等于当前时间的任务 null-不筛选；有值则筛选；
     * @return
     */
    Result<MarketingTask> getScoreTask(String date,Long taskId,Integer isTimeLimit,String jobNm);

    void JumpQueuehandle();
}
