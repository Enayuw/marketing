package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaRevokeRecord;
import com.br.marketing.entity.MarketingTcyrCpaRevokeRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaRevokeRecordMapperBase {
    int countByExample(MarketingTcyrCpaRevokeRecordExample example);

    int deleteByExample(MarketingTcyrCpaRevokeRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaRevokeRecord record);

    int insertSelective(MarketingTcyrCpaRevokeRecord record);

    List<MarketingTcyrCpaRevokeRecord> selectByExample(MarketingTcyrCpaRevokeRecordExample example);

    MarketingTcyrCpaRevokeRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaRevokeRecord record, @Param("example") MarketingTcyrCpaRevokeRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaRevokeRecord record, @Param("example") MarketingTcyrCpaRevokeRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaRevokeRecord record);

    int updateByPrimaryKey(MarketingTcyrCpaRevokeRecord record);
}