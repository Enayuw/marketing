package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.entity.MarketingTcyrSyncRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrSyncRecordMapperBase {
    int countByExample(MarketingTcyrSyncRecordExample example);

    int deleteByExample(MarketingTcyrSyncRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrSyncRecord record);

    int insertSelective(MarketingTcyrSyncRecord record);

    List<MarketingTcyrSyncRecord> selectByExample(MarketingTcyrSyncRecordExample example);

    MarketingTcyrSyncRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrSyncRecord record, @Param("example") MarketingTcyrSyncRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrSyncRecord record, @Param("example") MarketingTcyrSyncRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrSyncRecord record);

    int updateByPrimaryKey(MarketingTcyrSyncRecord record);
}