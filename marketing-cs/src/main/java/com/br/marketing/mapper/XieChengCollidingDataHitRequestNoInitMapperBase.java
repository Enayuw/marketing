package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataHitRequestNoInit;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoInitExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataHitRequestNoInitMapperBase {
    int countByExample(XieChengCollidingDataHitRequestNoInitExample example);

    int deleteByExample(XieChengCollidingDataHitRequestNoInitExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataHitRequestNoInit record);

    int insertSelective(XieChengCollidingDataHitRequestNoInit record);

    List<XieChengCollidingDataHitRequestNoInit> selectByExample(XieChengCollidingDataHitRequestNoInitExample example);

    XieChengCollidingDataHitRequestNoInit selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataHitRequestNoInit record,
                                 @Param("example") XieChengCollidingDataHitRequestNoInitExample example);

    int updateByExample(@Param("record") XieChengCollidingDataHitRequestNoInit record,
                        @Param("example") XieChengCollidingDataHitRequestNoInitExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataHitRequestNoInit record);

    int updateByPrimaryKey(XieChengCollidingDataHitRequestNoInit record);
}