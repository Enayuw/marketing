package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataFront;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaCollidingDataFrontMapper extends WubaCollidingDataFrontMapperBase {
    List<WubaCollidingDataFront> selectNoDupDataByLocalIdtikv_(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                                               @Param("minId") Long minId, @Param("pageSize") Integer pageSize);
    List<WubaCollidingDataFront> selectNoDupDataByCurDatetikv_(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                                               @Param("minId") Long minId, @Param("pageSize") Integer pageSize,
                                                               @Param("today") Date today, @Param("tomorrow") Date tomorrow,
                                                               @Param("highValueIds") String highValueIds,
                                                               @Param("reavedFileIds") String reavedFileIds);

    void batchUpdatePushStatusByCell(@Param("list") List<WubaCollidingDataFront> wubaCollidingDataFronts,
                                     @Param("localId") Long localId, @Param("apiCode") String apiCode);

    int updatePushStatusByHighValueFileIds(@Param("highValueIds") String highValueIds);
}
