package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRuleCenterHaloCallbackData;
import com.br.marketing.entity.MarketingRuleCenterHaloCallbackDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingRuleCenterHaloCallbackDataMapperBase {
    int countByExample(MarketingRuleCenterHaloCallbackDataExample example);

    int deleteByExample(MarketingRuleCenterHaloCallbackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingRuleCenterHaloCallbackData record);

    int insertSelective(MarketingRuleCenterHaloCallbackData record);

    List<MarketingRuleCenterHaloCallbackData> selectByExample(MarketingRuleCenterHaloCallbackDataExample example);

    MarketingRuleCenterHaloCallbackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingRuleCenterHaloCallbackData record, @Param("example") MarketingRuleCenterHaloCallbackDataExample example);

    int updateByExample(@Param("record") MarketingRuleCenterHaloCallbackData record, @Param("example") MarketingRuleCenterHaloCallbackDataExample example);

    int updateByPrimaryKeySelective(MarketingRuleCenterHaloCallbackData record);

    int updateByPrimaryKey(MarketingRuleCenterHaloCallbackData record);
}