package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.entity.MarketingDataFileConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingDataFileConfigMapperBase {
    long countByExample(MarketingDataFileConfigExample example);

    int deleteByExample(MarketingDataFileConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataFileConfig record);

    int insertSelective(MarketingDataFileConfig record);

    List<MarketingDataFileConfig> selectByExample(MarketingDataFileConfigExample example);

    MarketingDataFileConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataFileConfig record, @Param("example") MarketingDataFileConfigExample example);

    int updateByExample(@Param("record") MarketingDataFileConfig record, @Param("example") MarketingDataFileConfigExample example);

    int updateByPrimaryKeySelective(MarketingDataFileConfig record);

    int updateByPrimaryKey(MarketingDataFileConfig record);
}