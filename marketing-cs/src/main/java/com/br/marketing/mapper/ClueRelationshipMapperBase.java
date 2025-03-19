package com.br.marketing.mapper;

import com.br.marketing.entity.ClueRelationship;
import com.br.marketing.entity.ClueRelationshipExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClueRelationshipMapperBase {
    int countByExample(ClueRelationshipExample example);

    int deleteByExample(ClueRelationshipExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ClueRelationship record);

    int insertSelective(ClueRelationship record);

    List<ClueRelationship> selectByExample(ClueRelationshipExample example);

    ClueRelationship selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ClueRelationship record, @Param("example") ClueRelationshipExample example);

    int updateByExample(@Param("record") ClueRelationship record, @Param("example") ClueRelationshipExample example);

    int updateByPrimaryKeySelective(ClueRelationship record);

    int updateByPrimaryKey(ClueRelationship record);
}