package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.TaskStatus;

public interface MarketingTaskOptService {
    Result pauseTask(Long fileId, Integer isOrPause);
    Result pauseTaskByStraHisFile(Integer pauseType, StraHisFile straHisFile, TaskStatus taskStatus);

}
