package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataRob;
import com.br.marketing.entity.XieChengCpsCollidingDataRobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCpsCollidingDataRobMapperBase {
    int countByExample(XieChengCpsCollidingDataRobExample example);

    int deleteByExample(XieChengCpsCollidingDataRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCpsCollidingDataRob record);

    int insertSelective(XieChengCpsCollidingDataRob record);

    List<XieChengCpsCollidingDataRob> selectByExample(XieChengCpsCollidingDataRobExample example);

    XieChengCpsCollidingDataRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCpsCollidingDataRob record, @Param("example") XieChengCpsCollidingDataRobExample example);

    int updateByExample(@Param("record") XieChengCpsCollidingDataRob record, @Param("example") XieChengCpsCollidingDataRobExample example);

    int updateByPrimaryKeySelective(XieChengCpsCollidingDataRob record);

    int updateByPrimaryKey(XieChengCpsCollidingDataRob record);
}