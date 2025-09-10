package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaRob;
import com.br.marketing.entity.MarketingTcyrCpaRobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaRobMapperBase {
    int countByExample(MarketingTcyrCpaRobExample example);

    int deleteByExample(MarketingTcyrCpaRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaRob record);

    int insertSelective(MarketingTcyrCpaRob record);

    List<MarketingTcyrCpaRob> selectByExample(MarketingTcyrCpaRobExample example);

    MarketingTcyrCpaRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaRob record, @Param("example") MarketingTcyrCpaRobExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaRob record, @Param("example") MarketingTcyrCpaRobExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaRob record);

    int updateByPrimaryKey(MarketingTcyrCpaRob record);
}