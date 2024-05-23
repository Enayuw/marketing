package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingCleanDataTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingCleanDataTaskMapperBase {
    long countByExample(MarketingCleanDataTaskExample example);

    int deleteByExample(MarketingCleanDataTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCleanDataTask record);

    int insertSelective(MarketingCleanDataTask record);

    List<MarketingCleanDataTask> selectByExample(MarketingCleanDataTaskExample example);

    MarketingCleanDataTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCleanDataTask record, @Param("example") MarketingCleanDataTaskExample example);

    int updateByExample(@Param("record") MarketingCleanDataTask record, @Param("example") MarketingCleanDataTaskExample example);

    int updateByPrimaryKeySelective(MarketingCleanDataTask record);

    int updateByPrimaryKey(MarketingCleanDataTask record);
}