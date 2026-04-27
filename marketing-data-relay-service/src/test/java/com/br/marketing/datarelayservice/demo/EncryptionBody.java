package com.br.marketing.datarelayservice.demo;

import lombok.Data;

/**
 * 通用加密请求/响应体（aesKey、data、sign）
 */
@Data
public class EncryptionBody {

    /** AES密钥密文 */
    private String aesKey;
    /** 加密业务信息 */
    private String data;
    /** 签名 */
    private String sign;
}
