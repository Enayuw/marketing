package com.br.marketing.dto.xyf;

import lombok.Data;

/**
 * 通用加密主体类
 */

@Data
public class XyfEncryptionDTO{

    public XyfEncryptionDTO() {}

    public XyfEncryptionDTO(String aesKey, String data, String sign) {
        this.aesKey = aesKey;
        this.data = data;
        this.sign = sign;
    }

    /**
     * AES密钥密文
     */
    private String aesKey;
    /**
     * 加密业务信息
     */
    private String data;
    /**
     * 签名
     */
    private String sign;

}
