package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomizeDataValidConfig;
import com.br.marketing.entity.MarketingCustomizeDataValidConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCustomizeDataValidConfigMapperBase {
    int countByExample(MarketingCustomizeDataValidConfigExample example);

    int deleteByExample(MarketingCustomizeDataValidConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCustomizeDataValidConfig record);

    int insertSelective(MarketingCustomizeDataValidConfig record);

    List<MarketingCustomizeDataValidConfig> selectByExample(MarketingCustomizeDataValidConfigExample example);

    MarketingCustomizeDataValidConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCustomizeDataValidConfig record,
                                 @Param("example") MarketingCustomizeDataValidConfigExample example);

    int updateByExample(@Param("record") MarketingCustomizeDataValidConfig record,
                        @Param("example") MarketingCustomizeDataValidConfigExample example);

    int updateByPrimaryKeySelective(MarketingCustomizeDataValidConfig record);

    int updateByPrimaryKey(MarketingCustomizeDataValidConfig record);
}