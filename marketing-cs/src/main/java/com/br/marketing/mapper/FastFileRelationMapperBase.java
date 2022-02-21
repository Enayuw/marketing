package com.br.marketing.mapper;

import com.br.marketing.entity.FastFileRelation;
import com.br.marketing.entity.FastFileRelationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FastFileRelationMapperBase {
    int countByExample(FastFileRelationExample example);

    int deleteByExample(FastFileRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FastFileRelation record);

    int insertSelective(FastFileRelation record);

    List<FastFileRelation> selectByExample(FastFileRelationExample example);

    FastFileRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FastFileRelation record, @Param("example") FastFileRelationExample example);

    int updateByExample(@Param("record") FastFileRelation record, @Param("example") FastFileRelationExample example);

    int updateByPrimaryKeySelective(FastFileRelation record);

    int updateByPrimaryKey(FastFileRelation record);
}