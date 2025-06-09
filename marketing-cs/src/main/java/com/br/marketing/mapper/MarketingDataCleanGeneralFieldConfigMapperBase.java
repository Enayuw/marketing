package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanGeneralFieldConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralFieldConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingDataCleanGeneralFieldConfigMapperBase {
    long countByExample(MarketingDataCleanGeneralFieldConfigExample example);

    int deleteByExample(MarketingDataCleanGeneralFieldConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataCleanGeneralFieldConfig record);

    int insertSelective(MarketingDataCleanGeneralFieldConfig record);

    List<MarketingDataCleanGeneralFieldConfig> selectByExample(MarketingDataCleanGeneralFieldConfigExample example);

    MarketingDataCleanGeneralFieldConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataCleanGeneralFieldConfig record, @Param("example") MarketingDataCleanGeneralFieldConfigExample example);

    int updateByExample(@Param("record") MarketingDataCleanGeneralFieldConfig record, @Param("example") MarketingDataCleanGeneralFieldConfigExample example);

    int updateByPrimaryKeySelective(MarketingDataCleanGeneralFieldConfig record);

    int updateByPrimaryKey(MarketingDataCleanGeneralFieldConfig record);
}