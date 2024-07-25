package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.entity.WubaCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataRobMapper extends WubaCollidingDataRobMapperBase {
    void batchSaveData(@Param("robs") List<WubaCollidingDataFront> robs, @Param("apiCode") String apiCode);

    List<WubaCollidingDataRob> selectCollidingData(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdatePushTimeById(@Param("robs") List<WubaCollidingDataRob> robs);

    void batchDeleteByCell(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);
}