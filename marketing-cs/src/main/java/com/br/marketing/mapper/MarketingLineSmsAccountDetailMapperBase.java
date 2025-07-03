package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineSmsAccountDetail;
import com.br.marketing.entity.MarketingLineSmsAccountDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineSmsAccountDetailMapperBase {
    int countByExample(MarketingLineSmsAccountDetailExample example);

    int deleteByExample(MarketingLineSmsAccountDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingLineSmsAccountDetail record);

    int insertSelective(MarketingLineSmsAccountDetail record);

    List<MarketingLineSmsAccountDetail> selectByExample(MarketingLineSmsAccountDetailExample example);

    MarketingLineSmsAccountDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingLineSmsAccountDetail record, @Param("example") MarketingLineSmsAccountDetailExample example);

    int updateByExample(@Param("record") MarketingLineSmsAccountDetail record, @Param("example") MarketingLineSmsAccountDetailExample example);

    int updateByPrimaryKeySelective(MarketingLineSmsAccountDetail record);

    int updateByPrimaryKey(MarketingLineSmsAccountDetail record);
}