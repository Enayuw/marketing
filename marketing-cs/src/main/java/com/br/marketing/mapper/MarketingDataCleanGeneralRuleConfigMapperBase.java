package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingDataCleanGeneralRuleConfigMapperBase {
    long countByExample(MarketingDataCleanGeneralRuleConfigExample example);

    int deleteByExample(MarketingDataCleanGeneralRuleConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataCleanGeneralRuleConfig record);

    int insertSelective(MarketingDataCleanGeneralRuleConfig record);

    List<MarketingDataCleanGeneralRuleConfig> selectByExample(MarketingDataCleanGeneralRuleConfigExample example);

    MarketingDataCleanGeneralRuleConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataCleanGeneralRuleConfig record, @Param("example") MarketingDataCleanGeneralRuleConfigExample example);

    int updateByExample(@Param("record") MarketingDataCleanGeneralRuleConfig record, @Param("example") MarketingDataCleanGeneralRuleConfigExample example);

    int updateByPrimaryKeySelective(MarketingDataCleanGeneralRuleConfig record);

    int updateByPrimaryKey(MarketingDataCleanGeneralRuleConfig record);
}