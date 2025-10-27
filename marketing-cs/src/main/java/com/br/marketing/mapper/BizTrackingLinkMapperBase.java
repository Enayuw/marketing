package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingLink;
import com.br.marketing.entity.BizTrackingLinkExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkMapperBase {
    int countByExample(BizTrackingLinkExample example);

    int deleteByExample(BizTrackingLinkExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingLink record);

    int insertSelective(BizTrackingLink record);

    List<BizTrackingLink> selectByExample(BizTrackingLinkExample example);

    BizTrackingLink selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingLink record, @Param("example") BizTrackingLinkExample example);

    int updateByExample(@Param("record") BizTrackingLink record, @Param("example") BizTrackingLinkExample example);

    int updateByPrimaryKeySelective(BizTrackingLink record);

    int updateByPrimaryKey(BizTrackingLink record);
}