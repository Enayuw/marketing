package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.CustomerSoleRuleVO;

import java.util.List;

public interface SoleStrategyService {

    /**
     * 数据去重 1-未重复；2-重复;
     * @param soleRuleVOS
     * @param syncUser
     * @return
     */
    Result<Integer> actionSole(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser);

    List<CustomerScoreRuleVO> matchScoreRule(List<CustomerScoreRuleVO> scoreRuleVos, String userType);

    Result<String> analysisCondition(String conditionVo);

    String analysisSimpleConditionPlus(String conditionStr,String date,String time);

    Result<List<String>> analysisConditions(String conditionVo);

    Result<String> analysisTransferConditions(String conditionStr,String date,String time);
    Result<String> analysisTransferConditionsByValidConfig(String conditionStr, List<MarketingDataValidConfig> configList, String time);
}
