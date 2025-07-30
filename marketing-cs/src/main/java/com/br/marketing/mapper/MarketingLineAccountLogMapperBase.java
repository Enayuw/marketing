package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineAccountLog;
import com.br.marketing.entity.MarketingLineAccountLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineAccountLogMapperBase {
    int countByExample(MarketingLineAccountLogExample example);

    int deleteByExample(MarketingLineAccountLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingLineAccountLog record);

    int insertSelective(MarketingLineAccountLog record);

    List<MarketingLineAccountLog> selectByExample(MarketingLineAccountLogExample example);

    MarketingLineAccountLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingLineAccountLog record, @Param("example") MarketingLineAccountLogExample example);

    int updateByExample(@Param("record") MarketingLineAccountLog record, @Param("example") MarketingLineAccountLogExample example);

    int updateByPrimaryKeySelective(MarketingLineAccountLog record);

    int updateByPrimaryKey(MarketingLineAccountLog record);
}