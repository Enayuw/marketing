package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 描述：： 中邮数据加密处理组装
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouClientData
 * @author: it-yml
 * @create: 2023-08-03 15:14
 * @Version 1.0
 * --------------------------------------
 **/

@Slf4j
@Service
@Data
public class ZhongYouClientData {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
    /**
     * 加密算法RSA
     */
    @Value("${api.zhongyou.keyAlgorithm:00}")
    String keyAlgorithm;

    /**
     * 签名算法
     */
    @Value("${api.zhongyou.signAlgorithms:00}")
    String signAlgorithms;

    /**
     * 中邮提供
     */
    @Value("${api.zhongyou.aesKey:00}")
    String aesKey;

    /**
     * 中邮提供
     */
    @Value("${api.zhongyou.aesIv:00}")
    String aesIv;
    /**
     * 中邮提供
     */
    @Value("${api.zhongyou.channelCode:00}")
    String channelCode;
    /**
     * 中邮公钥
     */
    @Value("${api.zhongyou.rsaPublicKey:00}")
    String rsaPublicKey;

    /**
     * 百融私钥
     */
    @Value("${api.zhongyou.rsaPrivateKey:00}")
    String rsaPrivateKey;

    @Value("${api.zhongyou.query:00}")
    String queryUrl;

    @Value("${api.zhongyou.download:00}")
    String downloadUrl;

    @Value("${api.zhongyou.isProxy:false}")
    Boolean isProxy;



    /**
     * 中邮接口列表查询数据组装
     *
     * @return
     */
    public Object fileNameListData(String fileDate) {
        JSONObject data = new JSONObject();
        try {
            String format = SDF.format(new Date());
            JSONObject requestData = new JSONObject();
            requestData.put("fileType", "OUTMARKETING");
            requestData.put("startTime", fileDate);
            requestData.put("endTime", fileDate);
            String encryptDataStr = encryptData(requestData.toString());
            String signDataStr = signData(requestData.toString());
            data.put("channelCode", channelCode);
            data.put("version", "1.0");
            data.put("requestTime", format);
            data.put("sysSign", signDataStr);
//            data.put("requestData", requestData.toString());
            data.put("requestData", encryptDataStr);
        } catch (Exception e) {
            log.error("中邮文件名称请求数据加密异常:{}", e);
        }
        return data;
    }

    public Object fileDownLoadData(String fileName) {
        JSONObject data = new JSONObject();
        try {
            String format = SDF.format(new Date());
            JSONObject requestData = new JSONObject();
            requestData.put("fileName", fileName);
            String encryptDataStr = encryptData(requestData.toString());
            String signDataStr = signData(requestData.toString());
            data.put("channelCode", channelCode);
            data.put("version", "1.0");
            data.put("requestTime", format);
            data.put("sysSign", signDataStr);
            data.put("requestData", encryptDataStr);
//            data.put("requestData", requestData.toString());
        } catch (Exception e) {
            log.error("中邮文件内容获取请求加密异常:{}", e);
        }

        return data;
    }

    /**
     * 对请求参数加签demo
     *
     * @param requestData
     * @return
     */
    private String signData(String requestData) throws Exception {
        //aes加密
        byte[] encryptDataByte = encrypt(requestData, aesKey, aesIv);

        //rsa加签
        return sign(encryptDataByte, rsaPrivateKey);
    }

    /**
     * 数据签名
     *
     * @param content
     * @param privateKey
     * @return
     */
    private String sign(byte[] content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
            KeyFactory keyf = KeyFactory.getInstance(keyAlgorithm);
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);
            Signature signature = Signature.getInstance(signAlgorithms);
            signature.initSign(priKey);
            signature.update(content);
            byte[] signed = signature.sign();
            return Base64.encodeBase64String(signed);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据签名异常", e);
        }
    }

    /**
     * 对请求参数加密demo
     *
     * @param requestData
     * @return
     * @throws Exception
     */
    private String encryptData(String requestData) throws Exception {
        //aes加密
        byte[] encryptDataByte = encrypt(requestData, aesKey, aesIv);

        //加密后的数据
        return Base64.encodeBase64String(encryptDataByte);
    }

    private static byte[] encrypt(String data, String encryptKey, String ivKeys) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = ivKeys.getBytes("utf-8");
        IvParameterSpec iv = new IvParameterSpec(raw);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes("utf-8"), "AES"), iv);
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
        return encryptedData;
    }

    private static String decrypt(String data, String encryptKey, String ivKeys) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = ivKeys.getBytes("utf-8");
        IvParameterSpec iv = new IvParameterSpec(raw);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptKey.getBytes("utf-8"), "AES"), iv);
        byte[] decryptedData = cipher.doFinal(Base64.decodeBase64(data));
        return new String(decryptedData, "UTF-8");
    }

    /**
     * 对响应参数解密demo
     *
     * @param responseData 响应的数据
     * @param sign         响应的签名
     * @return
     */
    public String decryptData(String responseData, String sign) throws Exception {

        //对sign进行签名验证
        if (!doCheck(responseData, sign, rsaPublicKey)) {
            log.error("报文验签失败");
            throw new Exception();
        }

        //使用AES解密后的数据
        String decryptData = null;
        try {
            decryptData = decrypt(responseData, aesKey, aesIv);
        } catch (Exception e) {
            log.error("请求参数解密失败:{}", e);
            throw e;
        }
        return decryptData;
    }

    /**
     * 验签
     *
     * @param content
     * @param sign
     * @param publicKey
     * @return
     */
    private boolean doCheck(String content, String sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decodeBase64(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature signature = Signature
                    .getInstance(signAlgorithms);

            signature.initVerify(pubKey);
            signature.update(Base64.decodeBase64(content));

            boolean bverify = signature.verify(Base64.decodeBase64(sign));
            return bverify;

        } catch (Exception e) {
            log.error("验签失败,内容为={}", e);
        }
        return false;
    }
}
