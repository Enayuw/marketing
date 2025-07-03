package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineSmsAccountLog;
import com.br.marketing.entity.MarketingLineSmsAccountLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineSmsAccountLogMapperBase {
    int countByExample(MarketingLineSmsAccountLogExample example);

    int deleteByExample(MarketingLineSmsAccountLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingLineSmsAccountLog record);

    int insertSelective(MarketingLineSmsAccountLog record);

    List<MarketingLineSmsAccountLog> selectByExample(MarketingLineSmsAccountLogExample example);

    MarketingLineSmsAccountLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingLineSmsAccountLog record, @Param("example") MarketingLineSmsAccountLogExample example);

    int updateByExample(@Param("record") MarketingLineSmsAccountLog record, @Param("example") MarketingLineSmsAccountLogExample example);

    int updateByPrimaryKeySelective(MarketingLineSmsAccountLog record);

    int updateByPrimaryKey(MarketingLineSmsAccountLog record);
}