package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTaskModelCheck;
import com.br.marketing.entity.MarketingTaskModelCheckExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTaskModelCheckMapperBase {
    int countByExample(MarketingTaskModelCheckExample example);

    int deleteByExample(MarketingTaskModelCheckExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTaskModelCheck record);

    int insertSelective(MarketingTaskModelCheck record);

    List<MarketingTaskModelCheck> selectByExample(MarketingTaskModelCheckExample example);

    MarketingTaskModelCheck selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTaskModelCheck record, @Param("example") MarketingTaskModelCheckExample example);

    int updateByExample(@Param("record") MarketingTaskModelCheck record, @Param("example") MarketingTaskModelCheckExample example);

    int updateByPrimaryKeySelective(MarketingTaskModelCheck record);

    int updateByPrimaryKey(MarketingTaskModelCheck record);
}
