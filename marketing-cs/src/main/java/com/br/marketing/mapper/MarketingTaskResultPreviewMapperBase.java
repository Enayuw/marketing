package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTaskResultPreview;
import com.br.marketing.entity.MarketingTaskResultPreviewExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTaskResultPreviewMapperBase {
    int countByExample(MarketingTaskResultPreviewExample example);

    int deleteByExample(MarketingTaskResultPreviewExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTaskResultPreview record);

    int insertSelective(MarketingTaskResultPreview record);

    List<MarketingTaskResultPreview> selectByExample(MarketingTaskResultPreviewExample example);

    MarketingTaskResultPreview selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTaskResultPreview record, @Param("example") MarketingTaskResultPreviewExample example);

    int updateByExample(@Param("record") MarketingTaskResultPreview record, @Param("example") MarketingTaskResultPreviewExample example);

    int updateByPrimaryKeySelective(MarketingTaskResultPreview record);

    int updateByPrimaryKey(MarketingTaskResultPreview record);
}