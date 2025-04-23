package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagDataFieldConfig;
import com.br.marketing.entity.tag.TagDataFieldConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDataFieldConfigMapperBase {
    long countByExample(TagDataFieldConfigExample example);

    int deleteByExample(TagDataFieldConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagDataFieldConfig record);

    int insertSelective(TagDataFieldConfig record);

    List<TagDataFieldConfig> selectByExample(TagDataFieldConfigExample example);

    TagDataFieldConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagDataFieldConfig record, @Param("example") TagDataFieldConfigExample example);

    int updateByExample(@Param("record") TagDataFieldConfig record, @Param("example") TagDataFieldConfigExample example);

    int updateByPrimaryKeySelective(TagDataFieldConfig record);

    int updateByPrimaryKey(TagDataFieldConfig record);
}