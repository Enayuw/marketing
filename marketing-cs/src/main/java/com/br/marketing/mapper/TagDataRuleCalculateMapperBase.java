package com.br.marketing.mapper;

import com.br.marketing.entity.tag.TagDataRuleCalculate;
import com.br.marketing.entity.tag.TagDataRuleCalculateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDataRuleCalculateMapperBase {
    long countByExample(TagDataRuleCalculateExample example);

    int deleteByExample(TagDataRuleCalculateExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagDataRuleCalculate record);

    int insertSelective(TagDataRuleCalculate record);

    List<TagDataRuleCalculate> selectByExample(TagDataRuleCalculateExample example);

    TagDataRuleCalculate selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagDataRuleCalculate record, @Param("example") TagDataRuleCalculateExample example);

    int updateByExample(@Param("record") TagDataRuleCalculate record, @Param("example") TagDataRuleCalculateExample example);

    int updateByPrimaryKeySelective(TagDataRuleCalculate record);

    int updateByPrimaryKey(TagDataRuleCalculate record);
}