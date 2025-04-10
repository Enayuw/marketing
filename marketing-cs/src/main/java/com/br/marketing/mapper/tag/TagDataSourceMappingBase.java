package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagDataSourceMapping;
import com.br.marketing.entity.tag.TagDataSourceMappingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDataSourceMappingBase {
    long countByExample(TagDataSourceMappingExample example);

    int deleteByExample(TagDataSourceMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagDataSourceMapping record);

    int insertSelective(TagDataSourceMapping record);

    List<TagDataSourceMapping> selectByExample(TagDataSourceMappingExample example);

    TagDataSourceMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagDataSourceMapping record, @Param("example") TagDataSourceMappingExample example);

    int updateByExample(@Param("record") TagDataSourceMapping record, @Param("example") TagDataSourceMappingExample example);

    int updateByPrimaryKeySelective(TagDataSourceMapping record);

    int updateByPrimaryKey(TagDataSourceMapping record);
}