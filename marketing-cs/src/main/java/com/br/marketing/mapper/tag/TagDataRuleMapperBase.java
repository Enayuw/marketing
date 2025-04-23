package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.entity.tag.TagDataRuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDataRuleMapperBase {
    long countByExample(TagDataRuleExample example);

    int deleteByExample(TagDataRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagDataRule record);

    int insertSelective(TagDataRule record);

    List<TagDataRule> selectByExample(TagDataRuleExample example);

    TagDataRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagDataRule record, @Param("example") TagDataRuleExample example);

    int updateByExample(@Param("record") TagDataRule record, @Param("example") TagDataRuleExample example);

    int updateByPrimaryKeySelective(TagDataRule record);

    int updateByPrimaryKey(TagDataRule record);
}