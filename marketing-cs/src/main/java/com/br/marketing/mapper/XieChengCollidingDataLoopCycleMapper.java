package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataLoopCycleMapper extends XieChengCollidingDataLoopCycleMapperBase {
    List<Map<String, String>> selectPerMinuteCounts();

    Integer selectTodayCycleCount();

    List<XieChengCollidingDataLoopCycle> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    /**
     * 根据id批量更新is_deleted = 1
     *
     * @param ids
     */
    int updateBatchByIdToIsDeleted(@Param("ids") List<Long> ids);
}