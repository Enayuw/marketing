package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiDataLoopCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface DiDiV5DataLoopCycleMapper extends DiDiV5DataLoopCycleMapperBase {

    List<DiDiDataLoopCycle> queryCollidingDataBySharding(@Param("limit") int limit,
                                                         @Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime);

    Integer queryCollidingDataAmount(@Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime);


    void updatePushTimeByIds(@Param("pushTime") Date pushTime, @Param("ids") List<Long> ids);


    List<String> selectUnpushedCells(@Param("cells") Set<String> cells, @Param("apiCode") String apiCode);
}