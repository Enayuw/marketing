package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengData;
import com.br.marketing.entity.XieChengDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengDataMapperBase {
    int countByExample(XieChengDataExample example);

    int deleteByExample(XieChengDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengData record);

    int insertSelective(XieChengData record);

    List<XieChengData> selectByExampleWithBLOBs(XieChengDataExample example);

    List<XieChengData> selectByExample(XieChengDataExample example);

    XieChengData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengData record, @Param("example") XieChengDataExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengData record, @Param("example") XieChengDataExample example);

    int updateByExample(@Param("record") XieChengData record, @Param("example") XieChengDataExample example);

    int updateByPrimaryKeySelective(XieChengData record);

    int updateByPrimaryKeyWithBLOBs(XieChengData record);

    int updateByPrimaryKey(XieChengData record);
}