package com.br.marketing.client.smy.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 萨摩耶 client
 * @Author bin.li1
 * @CreateTime 2024-12-19
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmyCommReqDto {
    /**
     * 接口版本号 加密版本取值4.0
     */
    private String version;
    /**
     *流水号，不能重复。最长32个字符(数字+字母)。
     */
    private String reqSeqNumber;
    /**
     *商户号。萨摩耶提供
     */
    private String merchantNo;
    /**
     *时间戳。格式为：yyyy-MM-dd HH:mm:ss
     */
    private String timestamp;
    /**
     *签名字符串。MD5WithRSA签名，请求发送方使用RSA私钥加密，接收方使用公钥解密。参考调用方签名，bizContent先加密，后签名
     */
    private String sign;
    /**
     *请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递.
     */
    private String bizContent;
    /**
     *RSA加密后的AES密钥
     */
    private String secretKey;
}

