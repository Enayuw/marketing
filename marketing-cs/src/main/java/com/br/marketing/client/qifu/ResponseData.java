package com.br.marketing.client.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.client.qifu.util.AESUtil;
import com.br.marketing.client.qifu.util.RSAUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 响应消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-20 16:11
 */
public class ResponseData<T extends BizData> implements Serializable {
    private static final long serialVersionUID = 8973475062053320043L;
    /**
     * 2023-09-20 19:55
     * 状态 成功:S,失败: F
     * 必填
     */
    private FlagEnum flag;
    /**
     * 2023-09-20 19:55
     * 返回码  见:码表
     * 必填
     */
    private String code;
    /**
     * 2023-09-20 19:55
     * code  对应的描述
     * 必填
     */
    private String msg;
    /**
     * 2023-09-20 19:55
     * 返回数据
     * 必填
     */
    private ResultDataObj<T> data;

    @SuppressWarnings("unused")
    public ResponseData() {
    }

    @SuppressWarnings("unused")
    private ResponseData(FlagEnum flag, String code, String msg, ResultDataObj<T> data) {
        this.flag = flag;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public FlagEnum getFlag() {
        return flag;
    }

    public void setFlag(FlagEnum flag) {
        this.flag = flag;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 2023-09-21 14:15
     * 解密业务数据
     *
     * @param qiFuPublicKey 奇富公钥
     * @param brPrivateKey  百融私钥
     * @param typeReference 序列化类型
     */
    public void decryptData(String qiFuPublicKey, String brPrivateKey, TypeReference<T> typeReference) {
        Assert.isTrue(Objects.nonNull(data), "返回数据为空");
        // step1. SHA256withRSA验签
        String originSign = data.getSign();
        Assert.isTrue(StringUtils.isNotBlank(originSign), "签名为空");
        JSONObject jsonObject = (JSONObject) JSON.toJSON(data);
        jsonObject.remove("t");
        String signAgain = RSAUtil.generateContent(jsonObject);
        boolean verifyResult = RSAUtil.verifySignByPublicKey(qiFuPublicKey, originSign, signAgain);
        Assert.isTrue(verifyResult, "验签失败");
        String originData = data.getBizData();
        Assert.isTrue(StringUtils.isNotBlank(originData), "业务数据为空");
        // step2. RSA解密（客户端公钥加密，服务端私钥解密）AESKey和IV
        String originKey = data.getEncryptKey();
        String originIv = data.getEncryptIV();
        String decryptKey = RSAUtil.decryptByPrivateKey(brPrivateKey, originKey);
        String decryptIv = RSAUtil.decryptByPrivateKey(brPrivateKey, originIv);
        // step3. AES-CBC解密业务数据
        String decryptData = new String(Base64.decodeBase64(AESUtil.decrypt(decryptKey, decryptIv, originData))
                , StandardCharsets.UTF_8);
        data.setT(JSON.parseObject(decryptData, typeReference));
    }

    public ResultDataObj<T> getData(String qiFuPublicKey, String brPrivateKey, TypeReference<T> typeReference) {
        decryptData(qiFuPublicKey, brPrivateKey, typeReference);
        return data;
    }

    public ResultDataObj<T> getData() {
        return data;
    }

    public void setData(ResultDataObj<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "flag=" + flag +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}