package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRetryRedis;
import com.br.marketing.entity.MarketingRetryRedisExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRetryRedisMapperBase {
    int countByExample(MarketingRetryRedisExample example);

    int deleteByExample(MarketingRetryRedisExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingRetryRedis record);

    int insertSelective(MarketingRetryRedis record);

    List<MarketingRetryRedis> selectByExample(MarketingRetryRedisExample example);

    MarketingRetryRedis selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingRetryRedis record, @Param("example") MarketingRetryRedisExample example);

    int updateByExample(@Param("record") MarketingRetryRedis record, @Param("example") MarketingRetryRedisExample example);

    int updateByPrimaryKeySelective(MarketingRetryRedis record);

    int updateByPrimaryKey(MarketingRetryRedis record);
}