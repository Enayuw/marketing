package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataLoopCycleMapper extends XieChengCollidingDataLoopCycleMapperBase {
    List<Map<String, Object>> selectPerMinuteCounts();

    Integer selectTodayCycleCount();

    List<XieChengCollidingDataLoopCycle> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    /**
     * 根据id批量更新is_deleted = 1
     * @param ids
     */
    int updateBatchByIdToIsDeleted(@Param("ids") List<Long> ids);

    List<XieChengCollidingDataLoopCycle> selectCycleByRetryCount(@Param("minId") Long minId, @Param("isLast") Boolean isLast,@Param("pageSize") Integer pageSize);

    List<XieChengCollidingDataLoopCycle> selectCycleDataByReleaseTime(@Param("minId") Long minId, @Param("releaseTime") Date releaseTime,@Param("pageSize") Integer pageSize);

    int updateBatchByIdOfRetryCount(@Param("ids") List<Long> ids);
    int updateBatchByIdOfTrueDataList(List<XieChengCollidingDataLoopCycle> cycles);
}