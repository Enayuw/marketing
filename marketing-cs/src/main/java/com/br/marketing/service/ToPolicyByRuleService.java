package com.br.marketing.service;

import com.br.marketing.entity.CustomerInfoPushMain;

/**
 * @ClassName ToPolicyByRuleService
 * @Author kongbx
 * @Date 2025/1/12 13:58
 */
public interface ToPolicyByRuleService {

    Integer queryExistError(Long id, Integer filterType);

    boolean mockSwitch(String apiCode, String filterType, String errorType);

    void makeUpPolicyData(CustomerInfoPushMain customerInfoPushMain, String switchType);
}
