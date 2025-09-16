package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRuleCenterMergePushData;
import com.br.marketing.entity.MarketingRuleCenterMergePushDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRuleCenterMergePushDataMapperBase {
    int countByExample(MarketingRuleCenterMergePushDataExample example);

    int deleteByExample(MarketingRuleCenterMergePushDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingRuleCenterMergePushData record);

    int insertSelective(MarketingRuleCenterMergePushData record);

    List<MarketingRuleCenterMergePushData> selectByExample(MarketingRuleCenterMergePushDataExample example);

    MarketingRuleCenterMergePushData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingRuleCenterMergePushData record, @Param("example") MarketingRuleCenterMergePushDataExample example);

    int updateByExample(@Param("record") MarketingRuleCenterMergePushData record, @Param("example") MarketingRuleCenterMergePushDataExample example);

    int updateByPrimaryKeySelective(MarketingRuleCenterMergePushData record);

    int updateByPrimaryKey(MarketingRuleCenterMergePushData record);
}