package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataCleanGeneralRuleConfigMapper extends MarketingDataCleanGeneralRuleConfigMapperBase{


    List<MarketingDataCleanGeneralRuleConfig> getRuleConfigList(@Param("apiCode")String apiCode,@Param("dataType")Integer dataType,
                                                                @Param("acceptType")Integer acceptType);


}
