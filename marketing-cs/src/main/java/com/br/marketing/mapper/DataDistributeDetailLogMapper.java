package com.br.marketing.mapper;


import com.br.marketing.entity.DataDistributeDetailLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DataDistributeDetailLogMapper extends DataDistributeDetailLogMapperBase {

    Set<String> getToDataDistributeInfoList(@Param("apiCode") String apiCode, @Param("cells") Set<String> cells);

    void insertBatch(@Param("list") List<DataDistributeDetailLog> list);

    Set<String> findDistributeLogCellSet(
            @Param("apiCode") String apiCode,
            @Param("distributeType") Integer distributeType,
            @Param("distributeDate") String distributeDate,
            @Param("cells") Set<String> cells,
            @Param("marketingDate") String marketingDate,
            @Param("userType") String userType
    );

    List<Long> findZhongAnLockingDataDistributeLog(@Param("apiCode") String apiCode, @Param("distributeType") Integer distributeType,
        @Param("distributeDate") String distributeDate, @Param("cell") String cell, @Param("userType") String userType, @Param("tag") String tag);
}