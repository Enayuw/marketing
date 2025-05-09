package com.br.marketing.service.clean.common;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataCleanService {

    Result<Boolean> customerDataJsonParse(String t);


    Map<String, MarketingDataCleanGeneralRuleConfig> getConfigRule(String apiCode, Integer dataType, Integer acceptType);


    Object getCleanResult(JSONObject jsonObject, MarketingDataCleanGeneralRuleConfig rule);


    void customUploadDataClean(MarketingDataCleanGeneralConfig config, List<String> appletDateList);


    void dataCleanHandler(JSONObject jsonObject, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList, MarketingPreUserDetailDTO marketingPreUserDetailDTO);

    }
