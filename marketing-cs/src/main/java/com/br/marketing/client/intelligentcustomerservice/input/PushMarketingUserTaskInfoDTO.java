package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PushMarketingUserTaskInfoDTO implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * 请求类型固定值
     */
    private String method;

    /**
     *数据集合ID；相同数据集合id则表示数据同属于一个数据集合；
     */
    private String batchNumber;

    /**
     *数据集合名称；未填写则将数据集合ID为数据集合名称
     */
    private String batchName;

    /**
     *触达策略唯一标识
     */
    private String strategyCode;

    /**
     *
     */
    private String isAutoRunStrategy;

    /**
     *请求唯一标识,相同为重复请求,不填或为空均不校验,请按需传入唯一请求标识
     */
    private String accessNumber;

    /**
     *数据集属性自定义字段
     */
    private PushMarketingExtendDataDTO extendData;

    /**
     *外呼数据
     */
    private List<PushMarketingUserDetailDTO> data;

    /**
     * 请求批次号：b_customer_info_push_main的主键id
     */
    private String taskId;
}
