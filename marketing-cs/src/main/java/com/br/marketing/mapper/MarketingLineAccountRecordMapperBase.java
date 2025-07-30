package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineAccountRecord;
import com.br.marketing.entity.MarketingLineAccountRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineAccountRecordMapperBase {
    int countByExample(MarketingLineAccountRecordExample example);

    int deleteByExample(MarketingLineAccountRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingLineAccountRecord record);

    int insertSelective(MarketingLineAccountRecord record);

    List<MarketingLineAccountRecord> selectByExample(MarketingLineAccountRecordExample example);

    MarketingLineAccountRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingLineAccountRecord record, @Param("example") MarketingLineAccountRecordExample example);

    int updateByExample(@Param("record") MarketingLineAccountRecord record, @Param("example") MarketingLineAccountRecordExample example);

    int updateByPrimaryKeySelective(MarketingLineAccountRecord record);

    int updateByPrimaryKey(MarketingLineAccountRecord record);
}