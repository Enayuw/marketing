package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.util.List;

@Data
public class PolicyRetryByRuleDTO {

    /**
     * 推送数据的id集合
     */
    private List<Long> ids;

    /**
     * 初始数据的infoid
     */
    private Long infoId;

    /**
     * 推送决策参数
     */
    private PushMarketingUserDTO pushMarketingUserDTO;
}
