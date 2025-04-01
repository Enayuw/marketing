package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagDataSourceConfig;
import com.br.marketing.entity.tag.TagDataSourceConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDataSourceConfigMapperBase {
    long countByExample(TagDataSourceConfigExample example);

    int deleteByExample(TagDataSourceConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagDataSourceConfig record);

    int insertSelective(TagDataSourceConfig record);

    List<TagDataSourceConfig> selectByExample(TagDataSourceConfigExample example);

    TagDataSourceConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagDataSourceConfig record, @Param("example") TagDataSourceConfigExample example);

    int updateByExample(@Param("record") TagDataSourceConfig record, @Param("example") TagDataSourceConfigExample example);

    int updateByPrimaryKeySelective(TagDataSourceConfig record);

    int updateByPrimaryKey(TagDataSourceConfig record);
}