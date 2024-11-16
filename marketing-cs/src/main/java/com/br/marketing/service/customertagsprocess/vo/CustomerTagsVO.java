package com.br.marketing.service.customertagsprocess.vo;

import lombok.Data;

@Data
public class CustomerTagsVO {

    /**
     * 校验类型
     * {@link com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue.CheckTypeEnum}
     */
    private Integer checkType;

    /**
     * 0-原文；1-MD5；2；SHA256
     * {@link com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue.PushJc3keyTypeEnum}
     */
    private Integer pushJc3keyType;

}
