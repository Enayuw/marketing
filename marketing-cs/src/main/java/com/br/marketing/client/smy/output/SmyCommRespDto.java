package com.br.marketing.client.smy.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 萨摩耶 client
 * @Author bin.li1
 * @CreateTime 2024-12-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmyCommRespDto {
    /**
     * 返回参数的集合，最大长度不限，除sign外所有返回参数(包括code, msg)都必须放在这个参数中传递.
     */
    private String bizContent;
    /**
     *签名字符串。RSAWithMD5签名，发送方RSA使用私钥加密，接收方使用公钥解密，第三方私钥由萨摩耶颁发
     */
    private String sign;

    /**
     *RSA加密后的AES密钥
     */
    private String secretKey;

}

