package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataFront;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataFrontMapper extends WubaCollidingDataFrontMapperBase {
    List<WubaCollidingDataFront> selectNoDupDataByCurDate(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                                          @Param("minId") Long minId, @Param("pageSize") Integer pageSize);

    void batchUpdatePushStatusByCell(@Param("list") List<WubaCollidingDataFront> wubaCollidingDataFronts, @Param("localId") Long localId);
}