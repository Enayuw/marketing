package com.br.marketing.mapper;

import com.br.marketing.entity.HaierCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierCollidingDataMapper extends HaierCollidingDataMapperBase {

    /**
     * 查询数据
     * @param localId localId
     * @param sendDate sendDate
     * @param pageSize pageSize
     * @return java.util.List<com.br.marketing.entity.HaierCollidingData> 查询到的数据
     */
    List<HaierCollidingData> selectByLocalId(@Param("localId") Long localId, @Param("sendDate") Integer sendDate,
        @Param("pageSize") Integer pageSize);
}