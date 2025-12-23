package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataCleanGeneralConfigMapperBase {
    int countByExample(MarketingDataCleanGeneralConfigExample example);

    int deleteByExample(MarketingDataCleanGeneralConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataCleanGeneralConfig record);

    int insertSelective(MarketingDataCleanGeneralConfig record);

    List<MarketingDataCleanGeneralConfig> selectByExample(MarketingDataCleanGeneralConfigExample example);

    MarketingDataCleanGeneralConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataCleanGeneralConfig record, @Param("example") MarketingDataCleanGeneralConfigExample example);

    int updateByExample(@Param("record") MarketingDataCleanGeneralConfig record, @Param("example") MarketingDataCleanGeneralConfigExample example);

    int updateByPrimaryKeySelective(MarketingDataCleanGeneralConfig record);

    int updateByPrimaryKey(MarketingDataCleanGeneralConfig record);
}