package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiV5CollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DiDiV5CollidingDataMapper extends DiDiV5CollidingDataMapperBase {
    List<DiDiV5CollidingData> queryCollidingData(@Param("limit") int limit, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    void batchUpdatePushStatusByCell(@Param("list") List<DiDiV5CollidingData> diDiV5CollidingData,
                                     @Param("localId") Long localId, @Param("apiCode") String apiCode);
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

    void updateCollidingTimeByIds(@Param("pushStatus") int pushStatus, @Param("collidingTime") Date collidingTime, @Param("ids") List<Long> ids);


    List<Long> queryCollidingFileIds(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    int getPushStatusCountByLocalId(@Param("fileId") Long fileId, @Param("pushStatus") int pushStatus, @Param("startTime") Date startTime, @Param(
            "endTime") Date endTime);

    List<DiDiV5CollidingData> selectNoDupDataByLocalIdtikv_(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                                               @Param("minId") Long minId, @Param("pageSize") Integer pageSize,
                                                            @Param("collidingDate") Date collidingDate);


    List<DiDiV5CollidingData> selectNoDupDataByDateAndIdtikv_(
            @Param("apiCode") String apiCode,
            @Param("collidingTime") Date collidingTime,
            @Param("minId") Long minId,
            @Param("pageSize") Integer pageSize);
}