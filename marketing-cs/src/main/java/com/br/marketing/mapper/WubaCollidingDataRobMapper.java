package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaCollidingDataFront;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaCollidingDataRobMapper extends WubaCollidingDataRobMapperBase {
    void batchSaveData(@Param("robs") List<WubaCollidingDataFront> robs, @Param("apiCode") String apiCode);

    List<WubaCollidingData> selectCollidingData(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdatePushTimeById(@Param("robs") List<WubaCollidingData> robs);

    void batchDeleteByCell(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);

    List<WubaCollidingData> selectHighValueCollidingData(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode,
                                                         @Param("nowDate") Date nowDate, @Param("fileNames") List<String> fileNames);
    void batchSaveTrueToFalseData(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);
}