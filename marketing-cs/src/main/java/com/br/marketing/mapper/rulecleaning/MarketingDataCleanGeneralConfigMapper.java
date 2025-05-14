package com.br.marketing.mapper.rulecleaning;

import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.mapper.MarketingDataCleanGeneralConfigMapperBase;

import java.util.List;

public interface MarketingDataCleanGeneralConfigMapper extends MarketingDataCleanGeneralConfigMapperBase {

    /**
     * 根据条件查询规则列表
     *
     * @param param 查询参数
     * @return 规则列表
     */
    List<MarketingDataCleanGeneralConfig> selectRuleList(MarketingDataCleanGeneralConfig param);

    /**
     * 统计符合条件的规则数量
     *
     * @param param 查询参数
     * @return 规则数量
     */
    long countRuleList(MarketingDataCleanGeneralConfig param);


    List<MarketingDataCleanGeneralConfig> getCustomUploadConfig(String apiCode);
}