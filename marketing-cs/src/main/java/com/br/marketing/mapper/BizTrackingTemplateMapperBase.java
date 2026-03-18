package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingTemplate;
import com.br.marketing.entity.BizTrackingTemplateExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingTemplateMapperBase {
    int countByExample(BizTrackingTemplateExample example);

    int deleteByExample(BizTrackingTemplateExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingTemplate record);

    int insertSelective(BizTrackingTemplate record);

    List<BizTrackingTemplate> selectByExample(BizTrackingTemplateExample example);

    BizTrackingTemplate selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingTemplate record, @Param("example") BizTrackingTemplateExample example);

    int updateByExample(@Param("record") BizTrackingTemplate record, @Param("example") BizTrackingTemplateExample example);

    int updateByPrimaryKeySelective(BizTrackingTemplate record);

    int updateByPrimaryKey(BizTrackingTemplate record);
}