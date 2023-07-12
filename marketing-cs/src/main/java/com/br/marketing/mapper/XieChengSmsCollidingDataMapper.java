package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataMapper extends XieChengSmsCollidingDataMapperBase{



    List<XieChengSmsCollidingData> selectByLocalId(@Param("localId") Long localId, @Param("minId") Long minId,@Param("endTime") String endTime);




    /**
     * 批量更新
     * @param list
     */
    void updateBatch(@Param("list") List<String> list);

}