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

    /**
     * 查询正常重试数据：is_delete = 0 and retry_count > 0 and retry_count < 3
     * 查询兜底重试数据：is_delete = 0 and retry_count = 3
     * @param minId
     * @param isLast
     * @param pageSize
     * @return
     */
    List<XieChengCollidingDataLoopCycle> selectCycleByRetryCount(@Param("minId") Long minId, @Param("isLast") Boolean isLast,@Param("pageSize") Integer pageSize);

    /**
     * 查询待撞数据：is_delete = 0 and retry_count = 0 and release_time<now()
     * @param minId
     * @param releaseTime
     * @param pageSize
     * @return
     */
    List<XieChengCollidingDataLoopCycle> selectCycleDataByReleaseTime(@Param("minId") Long minId, @Param("releaseTime") Date releaseTime,@Param("pageSize") Integer pageSize);

    /**
     * 更新重试次数
     * @param ids
     * @return
     */
    int updateBatchByIdOfRetryCount(@Param("ids") List<Long> ids);
}