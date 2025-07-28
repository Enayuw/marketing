package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.entity.MarketingSmsAccountRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSmsAccountRecordMapperBase {
    int countByExample(MarketingSmsAccountRecordExample example);

    int deleteByExample(MarketingSmsAccountRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSmsAccountRecord record);

    int insertSelective(MarketingSmsAccountRecord record);

    List<MarketingSmsAccountRecord> selectByExample(MarketingSmsAccountRecordExample example);

    MarketingSmsAccountRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSmsAccountRecord record, @Param("example") MarketingSmsAccountRecordExample example);

    int updateByExample(@Param("record") MarketingSmsAccountRecord record, @Param("example") MarketingSmsAccountRecordExample example);

    int updateByPrimaryKeySelective(MarketingSmsAccountRecord record);

    int updateByPrimaryKey(MarketingSmsAccountRecord record);
}