package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DiDiV5CollidingDataRobMapper extends DiDiV5CollidingDataRobMapperBase {

    void insertToRobAndUpdateFront(@Param("list") List<DiDiCollidingDataRob> diDiV5CollidingData);

    List<DiDiCollidingDataRob> queryCollidingDataBySharding(@Param("limit") int limit,
                                                            @Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime,
                                                            @Param("shardingTotalCount") int shardingTotalCount,
                                                            @Param("shardingItems") List<Integer> shardingItems);

    List<DiDiCollidingDataRob> queryUploadedData(@Param("limit") int limit,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime,
                                                 @Param("shardingTotalCount") int shardingTotalCount,
                                                 @Param("shardingItems") List<Integer> shardingItems);


    void updatePushTimeByIds(@Param("pushTime") Date pushTime, @Param("ids") List<Long> ids);

}