package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaDataLog;
import com.br.marketing.entity.MarketingTcyrCpaDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaDataLogMapperBase {
    int countByExample(MarketingTcyrCpaDataLogExample example);

    int deleteByExample(MarketingTcyrCpaDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaDataLog record);

    int insertSelective(MarketingTcyrCpaDataLog record);

    List<MarketingTcyrCpaDataLog> selectByExample(MarketingTcyrCpaDataLogExample example);

    MarketingTcyrCpaDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaDataLog record, @Param("example") MarketingTcyrCpaDataLogExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaDataLog record, @Param("example") MarketingTcyrCpaDataLogExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaDataLog record);

    int updateByPrimaryKey(MarketingTcyrCpaDataLog record);
}