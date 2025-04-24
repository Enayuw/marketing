package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrSyncMapperBase {
    int countByExample(MarketingTcyrSyncExample example);

    int deleteByExample(MarketingTcyrSyncExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrSync record);

    int insertSelective(MarketingTcyrSync record);

    List<MarketingTcyrSync> selectByExample(MarketingTcyrSyncExample example);

    MarketingTcyrSync selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrSync record, @Param("example") MarketingTcyrSyncExample example);

    int updateByExample(@Param("record") MarketingTcyrSync record, @Param("example") MarketingTcyrSyncExample example);

    int updateByPrimaryKeySelective(MarketingTcyrSync record);

    int updateByPrimaryKey(MarketingTcyrSync record);
}