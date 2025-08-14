package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaTransferRecord;
import com.br.marketing.entity.MarketingTcyrCpaTransferRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaTransferRecordMapperBase {
    int countByExample(MarketingTcyrCpaTransferRecordExample example);

    int deleteByExample(MarketingTcyrCpaTransferRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaTransferRecord record);

    int insertSelective(MarketingTcyrCpaTransferRecord record);

    List<MarketingTcyrCpaTransferRecord> selectByExample(MarketingTcyrCpaTransferRecordExample example);

    MarketingTcyrCpaTransferRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaTransferRecord record, @Param("example") MarketingTcyrCpaTransferRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaTransferRecord record, @Param("example") MarketingTcyrCpaTransferRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaTransferRecord record);

    int updateByPrimaryKey(MarketingTcyrCpaTransferRecord record);
}