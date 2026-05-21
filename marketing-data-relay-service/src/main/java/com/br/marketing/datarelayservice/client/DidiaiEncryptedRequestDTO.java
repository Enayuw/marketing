package com.br.marketing.datarelayservice.client;

import lombok.Data;

import java.io.Serializable;

/**
 * 滴滴 AI 上传接口在 HTTP 层的密文请求体数据载体。
 *
 * <p>对端可能仅发送其中一种字段承载 Base64 密文，也可能兼容历史字段命名。服务层按固定优先级从请求中取出密文：
 * 优先使用 data，其次 cipherText，最后 cipher。应用标识、时间戳、签名均不在本对象中传递，
 * 而是由控制器从请求头读取。
 *
 * <p>本类实现 java.io.Serializable 接口，便于序列化场景扩展，常规 Spring MVC JSON 绑定不依赖序列化版本号变更策略。
 *
 * @author yueping.bai
 */
@Data
public class DidiaiEncryptedRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主密文字段，通常为 Base64 编码的 AES 密文字节串文本。
     */
    private String data;

    /**
     * 与 data 含义相同的备用字段名，当对端使用 cipherText 命名时使用本字段。
     */
    private String cipherText;

    /**
     * 当整段 JSON 仅包含一个密文属性且属性名为 cipher 时使用本字段承载密文内容。
     */
    private String cipher;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }
}
