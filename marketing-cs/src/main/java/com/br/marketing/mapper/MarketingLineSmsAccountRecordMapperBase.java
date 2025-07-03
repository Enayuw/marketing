package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineSmsAccountRecord;
import com.br.marketing.entity.MarketingLineSmsAccountRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineSmsAccountRecordMapperBase {
    int countByExample(MarketingLineSmsAccountRecordExample example);

    int deleteByExample(MarketingLineSmsAccountRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingLineSmsAccountRecord record);

    int insertSelective(MarketingLineSmsAccountRecord record);

    List<MarketingLineSmsAccountRecord> selectByExample(MarketingLineSmsAccountRecordExample example);

    MarketingLineSmsAccountRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingLineSmsAccountRecord record, @Param("example") MarketingLineSmsAccountRecordExample example);

    int updateByExample(@Param("record") MarketingLineSmsAccountRecord record, @Param("example") MarketingLineSmsAccountRecordExample example);

    int updateByPrimaryKeySelective(MarketingLineSmsAccountRecord record);

    int updateByPrimaryKey(MarketingLineSmsAccountRecord record);
}