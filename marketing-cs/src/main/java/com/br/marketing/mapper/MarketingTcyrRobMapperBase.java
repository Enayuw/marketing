package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrRob;
import com.br.marketing.entity.MarketingTcyrRobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrRobMapperBase {
    int countByExample(MarketingTcyrRobExample example);

    int deleteByExample(MarketingTcyrRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrRob record);

    int insertSelective(MarketingTcyrRob record);

    List<MarketingTcyrRob> selectByExample(MarketingTcyrRobExample example);

    MarketingTcyrRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrRob record, @Param("example") MarketingTcyrRobExample example);

    int updateByExample(@Param("record") MarketingTcyrRob record, @Param("example") MarketingTcyrRobExample example);

    int updateByPrimaryKeySelective(MarketingTcyrRob record);

    int updateByPrimaryKey(MarketingTcyrRob record);
}