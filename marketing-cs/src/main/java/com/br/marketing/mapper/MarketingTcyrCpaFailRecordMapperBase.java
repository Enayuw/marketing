package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailRecord;
import com.br.marketing.entity.MarketingTcyrCpaFailRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailRecordMapperBase {
    int countByExample(MarketingTcyrCpaFailRecordExample example);

    int deleteByExample(MarketingTcyrCpaFailRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaFailRecord record);

    int insertSelective(MarketingTcyrCpaFailRecord record);

    List<MarketingTcyrCpaFailRecord> selectByExample(MarketingTcyrCpaFailRecordExample example);

    MarketingTcyrCpaFailRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaFailRecord record, @Param("example") MarketingTcyrCpaFailRecordExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaFailRecord record, @Param("example") MarketingTcyrCpaFailRecordExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaFailRecord record);

    int updateByPrimaryKey(MarketingTcyrCpaFailRecord record);
}