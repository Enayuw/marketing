package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.io.Serializable;

@Data
public class PushMarketingExtendDataDTO implements Serializable {

    public static final long serialVersionUID = 1;

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
}
