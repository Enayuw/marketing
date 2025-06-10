package com.br.marketing.mapper;

import com.br.marketing.entity.UMengTimingTask;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface UMengTimingTaskMapper extends UMengTimingTaskMapperBase{

    UMengTimingTask getTodayLastTask(@Param("localId") Long localId, @Param("apiCode") String apiCode);

    UMengTimingTask getDataByTaskId(@Param("taskId") String taskId);
}