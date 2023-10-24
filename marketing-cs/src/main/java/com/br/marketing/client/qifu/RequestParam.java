package com.br.marketing.client.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.qifu.util.AESUtil;
import com.br.marketing.client.qifu.util.RSAUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * 请求参数
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-20 16:20
 */

public class RequestParam implements Serializable {

    private static final long serialVersionUID = -6476151439168886180L;

    /**
     * 2023-09-20 16:33
     * appId
     * 必填
     * 加签
     */
    private String appId;
    /**
     * 2023-09-20 16:33
     * rsa key 通过合作方 公钥生成
     * 必填
     * 加签
     */
    private String encryptKey;
    /**
     * 2023-09-20 16:33
     * rsa iv 通过合作方 公钥生成
     * 必填
     * 加签
     */
    @SuppressWarnings("all")
    private String encryptIV;
    /**
     * 2023-09-20 16:33
     * 签名
     * 非必填
     * 非加签
     */
    private String sign;
    /**
     * 2023-09-20 16:33
     * 时间戳 到毫秒
     * 必填
     * 加签
     */
    private String timestamp;
    /**
     * 2023-09-20 16:33
     * rsa 加密业务数据
     * 必填
     * 加签
     */
    private String bizData;

    protected RequestParam() {
    }

    protected RequestParam(String appId, String encryptKey, String encryptIv, String sign, String timestamp, String bizData) {
        this.appId = appId;
        this.encryptKey = encryptKey;
        this.encryptIV = encryptIv;
        this.sign = sign;
        this.timestamp = timestamp;
        this.bizData = bizData;
    }

    /**
     * 2023-09-20 19:26
     * 构造请求
     *
     * @param appId         appId
     * @param bizData       业务数据
     * @param qiFuPublicKey 奇富公钥
     * @param brPrivateKey  百融私钥
     */
    public RequestParam(String appId, BizData bizData, String qiFuPublicKey, String brPrivateKey) {
        Assert.notNull(bizData, "业务数据不可为空");
        // step1. 生成随机的16位AESKey和IV
        String aesKey = RandomStringUtils.randomAlphanumeric(16);
        String iv = RandomStringUtils.randomAlphanumeric(16);
        // step2. AES-CBC加密业务数据
        String jsonData = JSON.toJSONString(bizData);
        this.bizData = AESUtil.encrypt(aesKey, iv
                , Base64.encodeBase64URLSafeString(jsonData.getBytes(StandardCharsets.UTF_8)));
        // step3. RSA加密（客户端公钥加密，服务端私钥解密）AESKey和IV
        Assert.hasLength(qiFuPublicKey, "合作方公钥不可为空");
        this.encryptKey = RSAUtil.encryptByPublicKey(qiFuPublicKey, aesKey);
        this.encryptIV = RSAUtil.encryptByPublicKey(qiFuPublicKey, iv);
        Assert.notNull(appId, "appId不可为空");
        this.appId = appId;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        // step4. SHA256withRSA加签
        JSONObject jsonObject = (JSONObject) JSON.toJSON(this);
        String signature = RSAUtil.generateContent(jsonObject);
        Assert.hasLength(brPrivateKey, "私钥不可为空");
        this.sign = RSAUtil.signByPrivateKey(brPrivateKey, signature);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getEncryptIV() {
        return encryptIV;
    }

    public void setEncryptIV(String encryptIV) {
        this.encryptIV = encryptIV;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBizData() {
        return bizData;
    }

    public void setBizData(String bizData) {
        this.bizData = bizData;
    }

    @Override
    public String toString() {
        return "RequestParam{" +
                "appId='" + appId + '\'' +
                ", encryptKey='" + encryptKey + '\'' +
                ", encryptIV='" + encryptIV + '\'' +
                ", sign='" + sign + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", bizData='" + bizData + '\'' +
                '}';
    }
}
