package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineAccountDetail;
import com.br.marketing.entity.MarketingLineAccountDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineAccountDetailMapperBase {
    int countByExample(MarketingLineAccountDetailExample example);

    int deleteByExample(MarketingLineAccountDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingLineAccountDetail record);

    int insertSelective(MarketingLineAccountDetail record);

    List<MarketingLineAccountDetail> selectByExample(MarketingLineAccountDetailExample example);

    MarketingLineAccountDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingLineAccountDetail record, @Param("example") MarketingLineAccountDetailExample example);

    int updateByExample(@Param("record") MarketingLineAccountDetail record, @Param("example") MarketingLineAccountDetailExample example);

    int updateByPrimaryKeySelective(MarketingLineAccountDetail record);

    int updateByPrimaryKey(MarketingLineAccountDetail record);
}