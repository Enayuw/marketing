package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessRecord;
import com.br.marketing.entity.MarketingTcyrCpaSuccessRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessRecordMapperBase {
    int countByExample(MarketingTcyrCpaSuccessRecordExample example);

    int deleteByExample(MarketingTcyrCpaSuccessRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaSuccessRecord record);

    int insertSelective(MarketingTcyrCpaSuccessRecord record);

    List<MarketingTcyrCpaSuccessRecord> selectByExample(MarketingTcyrCpaSuccessRecordExample example);

    MarketingTcyrCpaSuccessRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaSuccessRecord record, @Param("example") MarketingTcyrCpaSuccessRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaSuccessRecord record, @Param("example") MarketingTcyrCpaSuccessRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaSuccessRecord record);

    int updateByPrimaryKey(MarketingTcyrCpaSuccessRecord record);
}