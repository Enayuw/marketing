package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanConfig;
import com.br.marketing.entity.MarketingDataCleanConfigExample;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MarketingDataCleanConfigMapperBase {
    int countByExample(MarketingDataCleanConfigExample example);

    int deleteByExample(MarketingDataCleanConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataCleanConfig record);

    int insertSelective(MarketingDataCleanConfig record);

    List<MarketingDataCleanConfig> selectByExample(MarketingDataCleanConfigExample example);

    MarketingDataCleanConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataCleanConfig record, @Param("example") MarketingDataCleanConfigExample example);

    int updateByExample(@Param("record") MarketingDataCleanConfig record, @Param("example") MarketingDataCleanConfigExample example);

    int updateByPrimaryKeySelective(MarketingDataCleanConfig record);

    int updateByPrimaryKey(MarketingDataCleanConfig record);
}