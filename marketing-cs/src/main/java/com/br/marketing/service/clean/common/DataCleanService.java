package com.br.marketing.service.clean.common;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.rulecleaning.DataCleanDTO;
import com.br.marketing.client.rulecleaning.RuleCleaningResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;
import com.br.marketing.entity.MarketingSyncUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataCleanService {

    Result<Boolean> customerDataJsonParse(String t);

    Result<Boolean> commonDataJsonParse(String message);

    Map<String, MarketingDataCleanGeneralRuleConfig> getConfigRule(String apiCode, Integer systemType,
                                                                   Integer dataType, Integer acceptType,Integer status);


    Object getCleanResult(JSONObject jsonObject, MarketingDataCleanGeneralRuleConfig rule);


    void customUploadDataClean(MarketingDataCleanGeneralConfig config, List<String> appletDateList);


    void dataCleanHandler(JSONObject jsonObject, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                          MarketingPreUserDetailDTO marketingPreUserDetailDTO);

    void fileUploadDataClean(MarketingCleanDataFile cleanFile, MarketingDataCleanGeneralConfig config);

    void processBatchDataSync(List<String> batchLines, String[] headers,
                              List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                              String apiCode, String fileName, int startIndex,
                              Map<String, String> virtualHeadersMap, String fieldDelimiter);

    void fileUploadCleanPre(List<List<RuleCleaningResult>> ruleCleaningResultList, List<MarketingDataCleanGeneralRuleConfig> ruleList,
                            MarketingCleanDataFile marketingCleanDataFile,Integer actualNum);

    List<JSONObject> fileDataAssemble(List<String> batchLines, String[] headers, String fileName, int startIndex,
                                     Map<String, String> virtualHeadersMap, String fieldDelimiter);

    void uploadDetailCleanHandler(JSONObject jsonObject, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                  MarketingSyncUser marketingSyncUser);


    Result commonClean(DataCleanDTO dto);

}
