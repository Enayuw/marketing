package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface DiDiV5CollidingDataRobMapper extends DiDiV5CollidingDataRobMapperBase {

    void insertToRobAndUpdateFront(@Param("list") List<DiDiCollidingDataRob> diDiV5CollidingData);

    List<DiDiCollidingDataRob> queryCollidingDataBySharding(@Param("limit") int limit,
                                                            @Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime);


    List<DiDiCollidingDataRob> queryUploadedData(@Param("limit") int limit,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime);

    List<DiDiCollidingDataRob> queryCollidingDataIdx(@Param("limit") int limit,
                                                            @Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime, @Param("maxId") long maxId);

    List<DiDiCollidingDataRob> queryUploadedDataIdx(@Param("limit") int limit,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime, @Param("maxId") long maxId);

    int queryCollidingDataAmount(@Param("startTime") Date startTime);


    void updatePushTimeByIds(@Param("pushTime") Date pushTime, @Param("ids") List<Long> ids);

    List<String> selectUnpushedCells(@Param("cells") Set<String> cells, @Param("apiCode") String apiCode);

}