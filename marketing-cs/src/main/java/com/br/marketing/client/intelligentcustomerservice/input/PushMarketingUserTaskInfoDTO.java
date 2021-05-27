package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PushMarketingUserTaskInfoDTO implements Serializable {
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
    private String extendData;

    /**
     *模型英文名称
     */
    private String scoreName;

    /**
     *分值区间
     */
    private String scoreRange;

    /**
     *数量top
     */
    private String amountTop;

    /**
     *样本数量
     */
    private String sampleTotal;

    /**
     *外呼数据
     */
    private List<PushMarketingUserDetailDTO> data;
}
