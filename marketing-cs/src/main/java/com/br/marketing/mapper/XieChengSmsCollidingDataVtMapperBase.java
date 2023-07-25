package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingDataVt;
import com.br.marketing.entity.XieChengSmsCollidingDataVtExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataVtMapperBase {
    int countByExample(XieChengSmsCollidingDataVtExample example);

    int deleteByExample(XieChengSmsCollidingDataVtExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengSmsCollidingDataVt record);

    int insertSelective(XieChengSmsCollidingDataVt record);

    List<XieChengSmsCollidingDataVt> selectByExample(XieChengSmsCollidingDataVtExample example);

    XieChengSmsCollidingDataVt selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengSmsCollidingDataVt record, @Param("example") XieChengSmsCollidingDataVtExample example);

    int updateByExample(@Param("record") XieChengSmsCollidingDataVt record, @Param("example") XieChengSmsCollidingDataVtExample example);

    int updateByPrimaryKeySelective(XieChengSmsCollidingDataVt record);

    int updateByPrimaryKey(XieChengSmsCollidingDataVt record);
}