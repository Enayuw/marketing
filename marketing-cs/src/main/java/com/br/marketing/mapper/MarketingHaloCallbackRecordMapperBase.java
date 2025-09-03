package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingHaloCallbackRecord;
import com.br.marketing.entity.MarketingHaloCallbackRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingHaloCallbackRecordMapperBase {
    int countByExample(MarketingHaloCallbackRecordExample example);

    int deleteByExample(MarketingHaloCallbackRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingHaloCallbackRecord record);

    int insertSelective(MarketingHaloCallbackRecord record);

    List<MarketingHaloCallbackRecord> selectByExample(MarketingHaloCallbackRecordExample example);

    MarketingHaloCallbackRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingHaloCallbackRecord record, @Param("example") MarketingHaloCallbackRecordExample example);

    int updateByExample(@Param("record") MarketingHaloCallbackRecord record, @Param("example") MarketingHaloCallbackRecordExample example);

    int updateByPrimaryKeySelective(MarketingHaloCallbackRecord record);

    int updateByPrimaryKey(MarketingHaloCallbackRecord record);
}