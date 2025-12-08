package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingLinkNode;
import com.br.marketing.entity.BizTrackingLinkNodeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkNodeMapperBase {
    int countByExample(BizTrackingLinkNodeExample example);

    int deleteByExample(BizTrackingLinkNodeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingLinkNode record);

    int insertSelective(BizTrackingLinkNode record);

    List<BizTrackingLinkNode> selectByExample(BizTrackingLinkNodeExample example);

    BizTrackingLinkNode selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingLinkNode record, @Param("example") BizTrackingLinkNodeExample example);

    int updateByExample(@Param("record") BizTrackingLinkNode record, @Param("example") BizTrackingLinkNodeExample example);

    int updateByPrimaryKeySelective(BizTrackingLinkNode record);

    int updateByPrimaryKey(BizTrackingLinkNode record);
}