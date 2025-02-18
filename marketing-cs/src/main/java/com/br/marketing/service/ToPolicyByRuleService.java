package com.br.marketing.service;

import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.entity.CustomerInfoPushMain;

import java.util.List;

/**
 * @ClassName ToPolicyByRuleService
 * @Author kongbx
 * @Date 2025/1/12 13:58
 */
public interface ToPolicyByRuleService {

    Integer queryExistError(Long id, Integer filterType);

    boolean mockSwitch(String apiCode, String filterType, String errorType);

    void makeUpPolicyData(CustomerInfoPushMain customerInfoPushMain, String switchType);

    List<List<PushMarketingUserDetailDTO>> splitParam(String apiCode, List<PushMarketingUserDetailDTO> userDetailDTOS);
}
