package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingTemplateNode;
import com.br.marketing.entity.BizTrackingTemplateNodeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingTemplateNodeMapperBase {
    int countByExample(BizTrackingTemplateNodeExample example);

    int deleteByExample(BizTrackingTemplateNodeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingTemplateNode record);

    int insertSelective(BizTrackingTemplateNode record);

    List<BizTrackingTemplateNode> selectByExample(BizTrackingTemplateNodeExample example);

    BizTrackingTemplateNode selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingTemplateNode record, @Param("example") BizTrackingTemplateNodeExample example);

    int updateByExample(@Param("record") BizTrackingTemplateNode record, @Param("example") BizTrackingTemplateNodeExample example);

    int updateByPrimaryKeySelective(BizTrackingTemplateNode record);

    int updateByPrimaryKey(BizTrackingTemplateNode record);
}