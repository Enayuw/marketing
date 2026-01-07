package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiV5CollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5CollidingDataLogMapper extends DiDiV5CollidingDataLogMapperBase {

    List<DiDiV5CollidingDataLog> queryFailedData(@Param("lastId") Long lastId,
                                                 @Param("pageSize") int pageSize,
                                                 @Param("apiCode") String apiCode);

    int checkCell(@Param("cell") String cell);
}