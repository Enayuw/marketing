package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSampleRecord;
import com.br.marketing.entity.MarketingTcyrCpaSampleRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSampleRecordMapperBase {
    int countByExample(MarketingTcyrCpaSampleRecordExample example);

    int deleteByExample(MarketingTcyrCpaSampleRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaSampleRecord record);

    int insertSelective(MarketingTcyrCpaSampleRecord record);

    List<MarketingTcyrCpaSampleRecord> selectByExample(MarketingTcyrCpaSampleRecordExample example);

    MarketingTcyrCpaSampleRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaSampleRecord record, @Param("example") MarketingTcyrCpaSampleRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaSampleRecord record, @Param("example") MarketingTcyrCpaSampleRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaSampleRecord record);

    int updateByPrimaryKey(MarketingTcyrCpaSampleRecord record);
}