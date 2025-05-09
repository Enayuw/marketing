package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaCollidingDataDelayLoopCycle;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface WubaCollidingDataDelayLoopCycleMapper extends WubaCollidingDataDelayLoopCycleMapperBase {
    List<String> selectDuplicateData(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);

    void batchUpdatePushTimeById(@Param("datas") List<WubaCollidingData> data);
    void batchUpdateReleaseTimeByCell(@Param("list") List<WubaCollidingDataDelayLoopCycle> data);

    void batchDeleteByCell(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);

    void batchSaveData(@Param("list") List<WubaCollidingDataDelayLoopCycle> list);
}