package com.br.marketing.service;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.FastTaskRule;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.CustomerSoleRuleVO;

import java.util.List;

/**
 * 假装该接口是domain对象——规则
 */
public interface IRuleConfigService {
    /**
     * 获取去重规则
     * @param apiCode
     * @return
     */
    Result<List<CustomerSoleRuleVO>> getSoleConfig(String apiCode);

    /**
     * 获取跑分规则
     * @param apiCode
     * @return
     */
    Result<List<CustomerScoreRuleVO>> getScoreConfig(String apiCode);


    Result<List<FastTaskRule>> getFastTaskRule(String apiCode);

    Result checkFastTaskRule(FastTaskRule rule);

    /**
     * 获取当天的跑分规则
     * @return
     */
    Result<List<CustomerScoreRuleVO>> getScoreConfigNow();


    Result<List<CustomerScoreRuleVO>> getScoreConfigNow(List<Long> ruleIds, String apiCode);
}
