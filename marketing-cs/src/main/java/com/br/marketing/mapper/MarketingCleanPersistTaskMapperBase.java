package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCleanPersistTask;
import com.br.marketing.entity.MarketingCleanPersistTaskExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface MarketingCleanPersistTaskMapperBase {
    long countByExample(MarketingCleanPersistTaskExample example);

    int deleteByExample(MarketingCleanPersistTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCleanPersistTask record);

    int insertSelective(MarketingCleanPersistTask record);

    List<MarketingCleanPersistTask> selectByExample(MarketingCleanPersistTaskExample example);

    MarketingCleanPersistTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCleanPersistTask record, @Param("example") MarketingCleanPersistTaskExample example);

    int updateByExample(@Param("record") MarketingCleanPersistTask record, @Param("example") MarketingCleanPersistTaskExample example);

    int updateByPrimaryKeySelective(MarketingCleanPersistTask record);

    int updateByPrimaryKey(MarketingCleanPersistTask record);
}
