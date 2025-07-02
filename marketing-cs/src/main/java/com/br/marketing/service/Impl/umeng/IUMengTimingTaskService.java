package com.br.marketing.service.Impl.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.UMengTimingTask;

import java.time.LocalDateTime;

/***
 *
 */
public interface IUMengTimingTaskService {

    void insertSelective(UMengTimingTask uMengTimingTask);

    UMengTimingTask getTodayLastTask(Long localId, String apiCode);

    UMengTimingTask getDataByTaskId(String taskId);
}
