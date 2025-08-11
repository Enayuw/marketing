package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCpaTransferRecord;
import com.br.marketing.entity.MarketingCpaTransferRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCpaTransferRecordMapperBase {
    int countByExample(MarketingCpaTransferRecordExample example);

    int deleteByExample(MarketingCpaTransferRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCpaTransferRecord record);

    int insertSelective(MarketingCpaTransferRecord record);

    List<MarketingCpaTransferRecord> selectByExample(MarketingCpaTransferRecordExample example);

    MarketingCpaTransferRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCpaTransferRecord record, @Param("example") MarketingCpaTransferRecordExample example);

    int updateByExample(@Param("record") MarketingCpaTransferRecord record, @Param("example") MarketingCpaTransferRecordExample example);

    int updateByPrimaryKeySelective(MarketingCpaTransferRecord record);

    int updateByPrimaryKey(MarketingCpaTransferRecord record);
}