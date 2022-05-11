package com.br.marketing.task.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTask;

import java.util.List;

public interface ITaskService {

    void buildScoreTask(List<Long> scoreRuleIds);

    Result<MarketingTask> getScoreTask(String date,Long taskId);
}
