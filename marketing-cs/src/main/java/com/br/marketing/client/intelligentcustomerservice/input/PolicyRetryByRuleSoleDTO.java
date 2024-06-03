package com.br.marketing.client.intelligentcustomerservice.input;

import com.br.marketing.dto.DataDistributeLogBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
/**
 * 推决策参数去重DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PolicyRetryByRuleSoleDTO extends DataDistributeLogBase<PushMarketingUserDetailDTO> {


    /**
     * 推送数据的id集合
     */
    private List<Long> ids;

    /**
     * 初始数据的infoid
     */
    private Long infoId;
    /**
     * 客户apiCode
     */
    private String apiCode;
    /**
     *数据集合ID；相同数据集合id则表示数据同属于一个数据集合；
     */
    private String batchNumber;

    /**
     *触达策略唯一标识
     */
    private String strategyCode;


}
