package com.br.marketing.mapper;

import com.br.marketing.entity.tag.TagDataDetail;
import com.br.marketing.entity.tag.TagDataDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDataDetailMapperBase {
    long countByExample(TagDataDetailExample example);

    int deleteByExample(TagDataDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagDataDetail record);

    int insertSelective(TagDataDetail record);

    List<TagDataDetail> selectByExample(TagDataDetailExample example);

    TagDataDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagDataDetail record, @Param("example") TagDataDetailExample example);

    int updateByExample(@Param("record") TagDataDetail record, @Param("example") TagDataDetailExample example);

    int updateByPrimaryKeySelective(TagDataDetail record);

    int updateByPrimaryKey(TagDataDetail record);
}