package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingTemplateEdge;
import com.br.marketing.entity.BizTrackingTemplateEdgeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingTemplateEdgeMapperBase {
    int countByExample(BizTrackingTemplateEdgeExample example);

    int deleteByExample(BizTrackingTemplateEdgeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingTemplateEdge record);

    int insertSelective(BizTrackingTemplateEdge record);

    List<BizTrackingTemplateEdge> selectByExample(BizTrackingTemplateEdgeExample example);

    BizTrackingTemplateEdge selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingTemplateEdge record, @Param("example") BizTrackingTemplateEdgeExample example);

    int updateByExample(@Param("record") BizTrackingTemplateEdge record, @Param("example") BizTrackingTemplateEdgeExample example);

    int updateByPrimaryKeySelective(BizTrackingTemplateEdge record);

    int updateByPrimaryKey(BizTrackingTemplateEdge record);
}