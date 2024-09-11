package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaCollidingDataSecondLoopCycleMapper extends WubaCollidingDataSecondLoopCycleMapperBase{
    List<WubaCollidingData> selectCollidingData(@Param("pushTimeStart") Date pushTimeStart, @Param("pushTimeEnd") Date pushTimeEnd,
                                                @Param("apiCode") String apiCode,
                                                @Param("pageSize") Integer pageSize);

    void batchUpdatePushTimeById(@Param("datas") List<WubaCollidingData> data);
}