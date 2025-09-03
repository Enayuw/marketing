package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaValueless;
import com.br.marketing.entity.MarketingTcyrCpaValuelessExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaValuelessMapperBase {
    int countByExample(MarketingTcyrCpaValuelessExample example);

    int deleteByExample(MarketingTcyrCpaValuelessExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaValueless record);

    int insertSelective(MarketingTcyrCpaValueless record);

    List<MarketingTcyrCpaValueless> selectByExample(MarketingTcyrCpaValuelessExample example);

    MarketingTcyrCpaValueless selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaValueless record, @Param("example") MarketingTcyrCpaValuelessExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaValueless record, @Param("example") MarketingTcyrCpaValuelessExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaValueless record);

    int updateByPrimaryKey(MarketingTcyrCpaValueless record);
}