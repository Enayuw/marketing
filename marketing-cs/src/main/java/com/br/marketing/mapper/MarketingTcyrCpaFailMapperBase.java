package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFail;
import com.br.marketing.entity.MarketingTcyrCpaFailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailMapperBase {
    int countByExample(MarketingTcyrCpaFailExample example);

    int deleteByExample(MarketingTcyrCpaFailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaFail record);

    int insertSelective(MarketingTcyrCpaFail record);

    List<MarketingTcyrCpaFail> selectByExample(MarketingTcyrCpaFailExample example);

    MarketingTcyrCpaFail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaFail record, @Param("example") MarketingTcyrCpaFailExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaFail record, @Param("example") MarketingTcyrCpaFailExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaFail record);

    int updateByPrimaryKey(MarketingTcyrCpaFail record);
}