package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSampleRecord;
import com.br.marketing.entity.MarketingTcyrSampleRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrSampleRecordMapperBase {
    int countByExample(MarketingTcyrSampleRecordExample example);

    int deleteByExample(MarketingTcyrSampleRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrSampleRecord record);

    int insertSelective(MarketingTcyrSampleRecord record);

    List<MarketingTcyrSampleRecord> selectByExampleWithBLOBs(MarketingTcyrSampleRecordExample example);

    List<MarketingTcyrSampleRecord> selectByExample(MarketingTcyrSampleRecordExample example);

    MarketingTcyrSampleRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrSampleRecord record, @Param("example") MarketingTcyrSampleRecordExample example);

    int updateByExampleWithBLOBs(@Param("record") MarketingTcyrSampleRecord record, @Param("example") MarketingTcyrSampleRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrSampleRecord record, @Param("example") MarketingTcyrSampleRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrSampleRecord record);

    int updateByPrimaryKeyWithBLOBs(MarketingTcyrSampleRecord record);

    int updateByPrimaryKey(MarketingTcyrSampleRecord record);
}