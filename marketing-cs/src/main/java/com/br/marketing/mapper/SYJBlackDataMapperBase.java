package com.br.marketing.mapper;

import com.br.marketing.entity.SYJBlackData;
import com.br.marketing.entity.SYJBlackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SYJBlackDataMapperBase {
    int countByExample(SYJBlackDataExample example);

    int deleteByExample(SYJBlackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SYJBlackData record);

    int insertSelective(SYJBlackData record);

    List<SYJBlackData> selectByExample(SYJBlackDataExample example);

    SYJBlackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SYJBlackData record, @Param("example") SYJBlackDataExample example);

    int updateByExample(@Param("record") SYJBlackData record, @Param("example") SYJBlackDataExample example);

    int updateByPrimaryKeySelective(SYJBlackData record);

    int updateByPrimaryKey(SYJBlackData record);
}