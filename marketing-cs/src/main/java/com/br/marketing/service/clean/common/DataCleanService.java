package com.br.marketing.service.clean.common;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;

import java.util.List;
import java.util.Map;

public interface DataCleanService {

    Result<Boolean> customerDataJsonParse(String t);


    Map<String, String> getConfigRule(String apiCode, Integer dataType, Integer acceptType);


    Object getCleanResult(JSONObject jsonObject, String rule);


    void customUploadDataClean(MarketingDataCleanGeneralConfig config, List<String> appletDateList);
}
