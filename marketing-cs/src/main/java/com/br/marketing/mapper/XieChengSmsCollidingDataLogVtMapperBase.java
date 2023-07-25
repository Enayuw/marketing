package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingDataLogVt;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVtExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataLogVtMapperBase {
    int countByExample(XieChengSmsCollidingDataLogVtExample example);

    int deleteByExample(XieChengSmsCollidingDataLogVtExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengSmsCollidingDataLogVt record);

    int insertSelective(XieChengSmsCollidingDataLogVt record);

    List<XieChengSmsCollidingDataLogVt> selectByExample(XieChengSmsCollidingDataLogVtExample example);

    XieChengSmsCollidingDataLogVt selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengSmsCollidingDataLogVt record, @Param("example") XieChengSmsCollidingDataLogVtExample example);

    int updateByExample(@Param("record") XieChengSmsCollidingDataLogVt record, @Param("example") XieChengSmsCollidingDataLogVtExample example);

    int updateByPrimaryKeySelective(XieChengSmsCollidingDataLogVt record);

    int updateByPrimaryKey(XieChengSmsCollidingDataLogVt record);
}