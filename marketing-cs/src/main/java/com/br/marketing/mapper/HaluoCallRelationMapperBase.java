package com.br.marketing.mapper;

import com.br.marketing.entity.HaluoCallRelation;
import com.br.marketing.entity.HaluoCallRelationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaluoCallRelationMapperBase {
    int countByExample(HaluoCallRelationExample example);

    int deleteByExample(HaluoCallRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HaluoCallRelation record);

    int insertSelective(HaluoCallRelation record);

    List<HaluoCallRelation> selectByExample(HaluoCallRelationExample example);

    HaluoCallRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HaluoCallRelation record, @Param("example") HaluoCallRelationExample example);

    int updateByExample(@Param("record") HaluoCallRelation record, @Param("example") HaluoCallRelationExample example);

    int updateByPrimaryKeySelective(HaluoCallRelation record);

    int updateByPrimaryKey(HaluoCallRelation record);
}