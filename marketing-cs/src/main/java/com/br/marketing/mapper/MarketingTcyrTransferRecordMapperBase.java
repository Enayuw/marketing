package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.entity.MarketingTcyrTransferRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrTransferRecordMapperBase {
    int countByExample(MarketingTcyrTransferRecordExample example);

    int deleteByExample(MarketingTcyrTransferRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrTransferRecord record);

    int insertSelective(MarketingTcyrTransferRecord record);

    List<MarketingTcyrTransferRecord> selectByExample(MarketingTcyrTransferRecordExample example);

    MarketingTcyrTransferRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrTransferRecord record, @Param("example") MarketingTcyrTransferRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrTransferRecord record, @Param("example") MarketingTcyrTransferRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrTransferRecord record);

    int updateByPrimaryKey(MarketingTcyrTransferRecord record);
}