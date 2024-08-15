package com.br.marketing.mapper.clean;

import com.br.marketing.entity.clean.MarketingCleanCreateTaskRule;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRuleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCleanCreateTaskRuleMapperBase {
    int countByExample(MarketingCleanCreateTaskRuleExample example);

    int deleteByExample(MarketingCleanCreateTaskRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCleanCreateTaskRule record);

    int insertSelective(MarketingCleanCreateTaskRule record);

    List<MarketingCleanCreateTaskRule> selectByExample(MarketingCleanCreateTaskRuleExample example);

    MarketingCleanCreateTaskRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCleanCreateTaskRule record, @Param("example") MarketingCleanCreateTaskRuleExample example);

    int updateByExample(@Param("record") MarketingCleanCreateTaskRule record, @Param("example") MarketingCleanCreateTaskRuleExample example);

    int updateByPrimaryKeySelective(MarketingCleanCreateTaskRule record);

    int updateByPrimaryKey(MarketingCleanCreateTaskRule record);
}