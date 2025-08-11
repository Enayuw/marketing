package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCpaRevokeRecord;
import com.br.marketing.entity.MarketingCpaRevokeRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCpaRevokeRecordMapperBase {
    int countByExample(MarketingCpaRevokeRecordExample example);

    int deleteByExample(MarketingCpaRevokeRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCpaRevokeRecord record);

    int insertSelective(MarketingCpaRevokeRecord record);

    List<MarketingCpaRevokeRecord> selectByExample(MarketingCpaRevokeRecordExample example);

    MarketingCpaRevokeRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCpaRevokeRecord record, @Param("example") MarketingCpaRevokeRecordExample example);

    int updateByExample(@Param("record") MarketingCpaRevokeRecord record, @Param("example") MarketingCpaRevokeRecordExample example);

    int updateByPrimaryKeySelective(MarketingCpaRevokeRecord record);

    int updateByPrimaryKey(MarketingCpaRevokeRecord record);
}