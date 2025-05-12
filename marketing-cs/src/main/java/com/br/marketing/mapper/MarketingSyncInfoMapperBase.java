package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingSyncInfoMapperBase {
    int countByExample(MarketingSyncInfoExample example);

    int deleteByExample(MarketingSyncInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSyncInfo record);

    int insertSelective(MarketingSyncInfo record);

    List<MarketingSyncInfo> selectByExample(MarketingSyncInfoExample example);

    MarketingSyncInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSyncInfo record, @Param("example") MarketingSyncInfoExample example);

    int updateByExample(@Param("record") MarketingSyncInfo record, @Param("example") MarketingSyncInfoExample example);

    int updateByPrimaryKeySelective(MarketingSyncInfo record);

    int updateByPrimaryKey(MarketingSyncInfo record);
}