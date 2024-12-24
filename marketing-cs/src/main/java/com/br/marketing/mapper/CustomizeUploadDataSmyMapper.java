package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeUploadDataSmy;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomizeUploadDataSmyMapper extends CustomizeUploadDataSmyMapperBase {
    Long smyCleanCustomizedUploadDataOfMinId(@Param("apiCode") String apiCode, @Param("date") String date);

    List<CustomizeUploadDataSmy> smyCleanCustomizedUploadDataByMinId(@Param("apiCode") String apiCode, @Param("date") String date,
                                                                     @Param("minId") Long minId, @Param("limit") Integer limit);
}