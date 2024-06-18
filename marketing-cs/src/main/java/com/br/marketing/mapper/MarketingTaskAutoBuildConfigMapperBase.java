package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTaskAutoBuildConfig;
import com.br.marketing.entity.MarketingTaskAutoBuildConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTaskAutoBuildConfigMapperBase {
    int countByExample(MarketingTaskAutoBuildConfigExample example);

    int deleteByExample(MarketingTaskAutoBuildConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTaskAutoBuildConfig record);

    int insertSelective(MarketingTaskAutoBuildConfig record);

    List<MarketingTaskAutoBuildConfig> selectByExample(MarketingTaskAutoBuildConfigExample example);

    MarketingTaskAutoBuildConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTaskAutoBuildConfig record, @Param("example") MarketingTaskAutoBuildConfigExample example);

    int updateByExample(@Param("record") MarketingTaskAutoBuildConfig record, @Param("example") MarketingTaskAutoBuildConfigExample example);

    int updateByPrimaryKeySelective(MarketingTaskAutoBuildConfig record);

    int updateByPrimaryKey(MarketingTaskAutoBuildConfig record);
}