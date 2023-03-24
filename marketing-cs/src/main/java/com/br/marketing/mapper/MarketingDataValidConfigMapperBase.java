package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingDataValidConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataValidConfigMapperBase {
    int countByExample(MarketingDataValidConfigExample example);

    int deleteByExample(MarketingDataValidConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataValidConfig record);

    int insertSelective(MarketingDataValidConfig record);

    List<MarketingDataValidConfig> selectByExample(MarketingDataValidConfigExample example);

    MarketingDataValidConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataValidConfig record, @Param("example") MarketingDataValidConfigExample example);

    int updateByExample(@Param("record") MarketingDataValidConfig record, @Param("example") MarketingDataValidConfigExample example);

    int updateByPrimaryKeySelective(MarketingDataValidConfig record);

    int updateByPrimaryKey(MarketingDataValidConfig record);
}