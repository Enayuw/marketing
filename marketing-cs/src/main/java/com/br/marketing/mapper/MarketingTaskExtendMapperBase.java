package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.entity.MarketingTaskExtendExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTaskExtendMapperBase {
    int countByExample(MarketingTaskExtendExample example);

    int deleteByExample(MarketingTaskExtendExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTaskExtend record);

    int insertSelective(MarketingTaskExtend record);

    List<MarketingTaskExtend> selectByExample(MarketingTaskExtendExample example);

    MarketingTaskExtend selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTaskExtend record, @Param("example") MarketingTaskExtendExample example);

    int updateByExample(@Param("record") MarketingTaskExtend record, @Param("example") MarketingTaskExtendExample example);

    int updateByPrimaryKeySelective(MarketingTaskExtend record);

    int updateByPrimaryKey(MarketingTaskExtend record);
}