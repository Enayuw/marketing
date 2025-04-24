package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanRule;
import com.br.marketing.entity.MarketingDataCleanRuleExample;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MarketingDataCleanRuleMapperBase {
    int countByExample(MarketingDataCleanRuleExample example);

    int deleteByExample(MarketingDataCleanRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataCleanRule record);

    int insertSelective(MarketingDataCleanRule record);

    List<MarketingDataCleanRule> selectByExample(MarketingDataCleanRuleExample example);

    MarketingDataCleanRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataCleanRule record, @Param("example") MarketingDataCleanRuleExample example);

    int updateByExample(@Param("record") MarketingDataCleanRule record, @Param("example") MarketingDataCleanRuleExample example);

    int updateByPrimaryKeySelective(MarketingDataCleanRule record);

    int updateByPrimaryKey(MarketingDataCleanRule record);
}