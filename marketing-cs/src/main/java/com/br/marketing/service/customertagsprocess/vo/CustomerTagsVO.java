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

       /**
     * 加密模式
     */
    private String cipherMode;

    /**
     * 填充模式
     */
    private String paddingScheme;

    /**
     * 字符编码
     */
    private String charset;

    /**
     * 初始化向量
     */
    private String iv;

    /**
     * 密钥
     */
    private String dynamicKeys;

}
