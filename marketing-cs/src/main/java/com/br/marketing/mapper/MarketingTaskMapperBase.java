package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingTaskMapperBase {
    long countByExample(MarketingTaskExample example);

    int deleteByExample(MarketingTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTask record);

    int insertSelective(MarketingTask record);

    List<MarketingTask> selectByExample(MarketingTaskExample example);

    MarketingTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTask record, @Param("example") MarketingTaskExample example);

    int updateByExample(@Param("record") MarketingTask record, @Param("example") MarketingTaskExample example);

    int updateByPrimaryKeySelective(MarketingTask record);

    int updateByPrimaryKey(MarketingTask record);
}