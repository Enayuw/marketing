package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagRuleSourceRelation;
import com.br.marketing.entity.tag.TagRuleSourceRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagRuleSourceRelationMapperBase {
    long countByExample(TagRuleSourceRelationExample example);

    int deleteByExample(TagRuleSourceRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagRuleSourceRelation record);

    int insertSelective(TagRuleSourceRelation record);

    List<TagRuleSourceRelation> selectByExample(TagRuleSourceRelationExample example);

    TagRuleSourceRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagRuleSourceRelation record, @Param("example") TagRuleSourceRelationExample example);

    int updateByExample(@Param("record") TagRuleSourceRelation record, @Param("example") TagRuleSourceRelationExample example);

    int updateByPrimaryKeySelective(TagRuleSourceRelation record);

    int updateByPrimaryKey(TagRuleSourceRelation record);
}