package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSyncRecord;
import com.br.marketing.entity.MarketingTcyrCpaSyncRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSyncRecordMapperBase {
    int countByExample(MarketingTcyrCpaSyncRecordExample example);

    int deleteByExample(MarketingTcyrCpaSyncRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaSyncRecord record);

    int insertSelective(MarketingTcyrCpaSyncRecord record);

    List<MarketingTcyrCpaSyncRecord> selectByExample(MarketingTcyrCpaSyncRecordExample example);

    MarketingTcyrCpaSyncRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaSyncRecord record, @Param("example") MarketingTcyrCpaSyncRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaSyncRecord record, @Param("example") MarketingTcyrCpaSyncRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaSyncRecord record);

    int updateByPrimaryKey(MarketingTcyrCpaSyncRecord record);
}