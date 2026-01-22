package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiDataLoopCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DiDiV5DataLoopCycleMapper extends DiDiV5DataLoopCycleMapperBase {

    List<DiDiDataLoopCycle> queryCollidingDataBySharding(@Param("limit") int limit,
                                                         @Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime,
                                                         @Param("shardingTotalCount") int shardingTotalCount,
                                                         @Param("shardingItems") List<Integer> shardingItems);


    void updatePushTimeByIds(@Param("pushTime") Date pushTime, @Param("ids") List<Long> ids);
}