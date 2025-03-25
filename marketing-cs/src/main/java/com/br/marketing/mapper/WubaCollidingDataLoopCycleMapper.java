package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaCollidingDataLoopCycleMapper extends WubaCollidingDataLoopCycleMapperBase {
    void batchSaveData(@Param("list") List<String> list, @Param("apiCode") String apiCode, @Param("dataSourceType") String dataSourceType);

    List<WubaCollidingData> selectCollidingData(@Param("pushTimeStart") Date pushTimeStart, @Param("pushTimeEnd") Date pushTimeEnd,
                                                @Param("apiCode") String apiCode,
                                                @Param("pageSize") Integer pageSize);

    void batchUpdatePushTimeById(@Param("datas") List<WubaCollidingData> data);
    void batchDeleteByCell(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);
    List<String> selectDuplicateData(@Param("list") List<String> list, @Param("apiCode") String apiCode);
}
