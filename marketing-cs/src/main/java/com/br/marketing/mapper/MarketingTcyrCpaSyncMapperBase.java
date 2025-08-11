package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSync;
import com.br.marketing.entity.MarketingTcyrCpaSyncExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSyncMapperBase {
    int countByExample(MarketingTcyrCpaSyncExample example);

    int deleteByExample(MarketingTcyrCpaSyncExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaSync record);

    int insertSelective(MarketingTcyrCpaSync record);

    List<MarketingTcyrCpaSync> selectByExample(MarketingTcyrCpaSyncExample example);

    MarketingTcyrCpaSync selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaSync record, @Param("example") MarketingTcyrCpaSyncExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaSync record, @Param("example") MarketingTcyrCpaSyncExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaSync record);

    int updateByPrimaryKey(MarketingTcyrCpaSync record);
}