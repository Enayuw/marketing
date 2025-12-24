package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiV5CollidingData;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiDiV5CollidingDataMapper extends DiDiV5CollidingDataMapperBase {
    List<DiDiV5CollidingData> queryCollidingData(@Param("limit") int limit, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 分片查询撞库数据
     *
     * @param limit 每批查询数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param shardingTotalCount 总分片数
     * @param shardingItems 当前分片项列表
     * @return 撞库数据列表
     */
    List<DiDiV5CollidingData> queryCollidingDataBySharding(@Param("limit") int limit, 
                                                           @Param("startTime") Date startTime, 
                                                           @Param("endTime") Date endTime,
                                                           @Param("shardingTotalCount") int shardingTotalCount,
                                                           @Param("shardingItems") List<Integer> shardingItems);

    void updatePushStatusByIds(@Param("pushStatus") int pushStatus, @Param("ids") List<Long> ids);

    List<Long> queryCollidingFileIds(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    int getPushStatusCountByLocalId(@Param("fileId") Long fileId, @Param("pushStatus") int pushStatus, @Param("startTime") Date startTime, @Param(
            "endTime") Date endTime);
}