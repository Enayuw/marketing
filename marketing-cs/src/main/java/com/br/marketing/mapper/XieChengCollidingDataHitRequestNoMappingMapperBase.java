package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataHitRequestNoMapping;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataHitRequestNoMappingMapperBase {
    int countByExample(XieChengCollidingDataHitRequestNoMappingExample example);

    int deleteByExample(XieChengCollidingDataHitRequestNoMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataHitRequestNoMapping record);

    int insertSelective(XieChengCollidingDataHitRequestNoMapping record);

    List<XieChengCollidingDataHitRequestNoMapping> selectByExample(XieChengCollidingDataHitRequestNoMappingExample example);

    XieChengCollidingDataHitRequestNoMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataHitRequestNoMapping record,
                                 @Param("example") XieChengCollidingDataHitRequestNoMappingExample example);

    int updateByExample(@Param("record") XieChengCollidingDataHitRequestNoMapping record,
                        @Param("example") XieChengCollidingDataHitRequestNoMappingExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataHitRequestNoMapping record);

    int updateByPrimaryKey(XieChengCollidingDataHitRequestNoMapping record);
}