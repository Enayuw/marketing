package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncErrorInfo;
import com.br.marketing.entity.MarketingSyncErrorInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSyncErrorInfoMapperBase {
    int countByExample(MarketingSyncErrorInfoExample example);

    int deleteByExample(MarketingSyncErrorInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSyncErrorInfo record);

    int insertSelective(MarketingSyncErrorInfo record);

    List<MarketingSyncErrorInfo> selectByExampleWithBLOBs(MarketingSyncErrorInfoExample example);

    List<MarketingSyncErrorInfo> selectByExample(MarketingSyncErrorInfoExample example);

    MarketingSyncErrorInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSyncErrorInfo record, @Param("example") MarketingSyncErrorInfoExample example);

    int updateByExampleWithBLOBs(@Param("record") MarketingSyncErrorInfo record, @Param("example") MarketingSyncErrorInfoExample example);

    int updateByExample(@Param("record") MarketingSyncErrorInfo record, @Param("example") MarketingSyncErrorInfoExample example);

    int updateByPrimaryKeySelective(MarketingSyncErrorInfo record);

    int updateByPrimaryKeyWithBLOBs(MarketingSyncErrorInfo record);

    int updateByPrimaryKey(MarketingSyncErrorInfo record);
}