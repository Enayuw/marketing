package com.br.marketing.client.intelligentcustomerservice.input;

import com.br.marketing.dto.DataDistributeLogBase;
import lombok.Data;

import java.util.List;
/**
 * 推决策参数去重DTO
 */
@Data
public class PolicyRetryByRuleSoleDTO extends DataDistributeLogBase<PushMarketingUserDetailByRuleDTO> {


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
