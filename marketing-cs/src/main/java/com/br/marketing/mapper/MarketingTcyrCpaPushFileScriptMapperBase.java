package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaPushFileScript;
import com.br.marketing.entity.MarketingTcyrCpaPushFileScriptExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaPushFileScriptMapperBase {
    int countByExample(MarketingTcyrCpaPushFileScriptExample example);

    int deleteByExample(MarketingTcyrCpaPushFileScriptExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaPushFileScript record);

    int insertSelective(MarketingTcyrCpaPushFileScript record);

    List<MarketingTcyrCpaPushFileScript> selectByExample(MarketingTcyrCpaPushFileScriptExample example);

    MarketingTcyrCpaPushFileScript selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaPushFileScript record, @Param("example") MarketingTcyrCpaPushFileScriptExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaPushFileScript record, @Param("example") MarketingTcyrCpaPushFileScriptExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaPushFileScript record);

    int updateByPrimaryKey(MarketingTcyrCpaPushFileScript record);
}