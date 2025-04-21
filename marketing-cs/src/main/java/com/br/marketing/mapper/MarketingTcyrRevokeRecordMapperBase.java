package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrRevokeRecord;
import com.br.marketing.entity.MarketingTcyrRevokeRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrRevokeRecordMapperBase {
    int countByExample(MarketingTcyrRevokeRecordExample example);

    int deleteByExample(MarketingTcyrRevokeRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrRevokeRecord record);

    int insertSelective(MarketingTcyrRevokeRecord record);

    List<MarketingTcyrRevokeRecord> selectByExample(MarketingTcyrRevokeRecordExample example);

    MarketingTcyrRevokeRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrRevokeRecord record, @Param("example") MarketingTcyrRevokeRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrRevokeRecord record, @Param("example") MarketingTcyrRevokeRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrRevokeRecord record);

    int updateByPrimaryKey(MarketingTcyrRevokeRecord record);
}