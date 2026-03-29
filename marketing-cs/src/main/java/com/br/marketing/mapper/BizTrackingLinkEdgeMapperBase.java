package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingLinkEdge;
import com.br.marketing.entity.BizTrackingLinkEdgeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkEdgeMapperBase {
    int countByExample(BizTrackingLinkEdgeExample example);

    int deleteByExample(BizTrackingLinkEdgeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingLinkEdge record);

    int insertSelective(BizTrackingLinkEdge record);

    List<BizTrackingLinkEdge> selectByExample(BizTrackingLinkEdgeExample example);

    BizTrackingLinkEdge selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingLinkEdge record, @Param("example") BizTrackingLinkEdgeExample example);

    int updateByExample(@Param("record") BizTrackingLinkEdge record, @Param("example") BizTrackingLinkEdgeExample example);

    int updateByPrimaryKeySelective(BizTrackingLinkEdge record);

    int updateByPrimaryKey(BizTrackingLinkEdge record);
}