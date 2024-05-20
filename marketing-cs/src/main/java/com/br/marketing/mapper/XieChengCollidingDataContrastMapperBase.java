package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataContrastExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataContrastMapperBase {
    int countByExample(XieChengCollidingDataContrastExample example);

    int deleteByExample(XieChengCollidingDataContrastExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataContrast record);

    int insertSelective(XieChengCollidingDataContrast record);

    List<XieChengCollidingDataContrast> selectByExampleWithBLOBs(XieChengCollidingDataContrastExample example);

    List<XieChengCollidingDataContrast> selectByExample(XieChengCollidingDataContrastExample example);

    XieChengCollidingDataContrast selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataContrast record, @Param("example") XieChengCollidingDataContrastExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengCollidingDataContrast record, @Param("example") XieChengCollidingDataContrastExample example);

    int updateByExample(@Param("record") XieChengCollidingDataContrast record, @Param("example") XieChengCollidingDataContrastExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataContrast record);

    int updateByPrimaryKeyWithBLOBs(XieChengCollidingDataContrast record);

    int updateByPrimaryKey(XieChengCollidingDataContrast record);
}