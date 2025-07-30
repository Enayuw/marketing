package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSmsAccountDetail;
import com.br.marketing.entity.MarketingSmsAccountDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSmsAccountDetailMapperBase {
    int countByExample(MarketingSmsAccountDetailExample example);

    int deleteByExample(MarketingSmsAccountDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSmsAccountDetail record);

    int insertSelective(MarketingSmsAccountDetail record);

    List<MarketingSmsAccountDetail> selectByExample(MarketingSmsAccountDetailExample example);

    MarketingSmsAccountDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSmsAccountDetail record, @Param("example") MarketingSmsAccountDetailExample example);

    int updateByExample(@Param("record") MarketingSmsAccountDetail record, @Param("example") MarketingSmsAccountDetailExample example);

    int updateByPrimaryKeySelective(MarketingSmsAccountDetail record);

    int updateByPrimaryKey(MarketingSmsAccountDetail record);
}