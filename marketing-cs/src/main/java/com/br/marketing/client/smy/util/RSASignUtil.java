package com.br.marketing.client.smy.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by wangjianqiang on 2018/1/8.
 * 加密方：原文->求MD5->RSA私钥加密->base64编码字符串（密文）
 * 解密方：密文->base64解码->RSA公钥钥解密->原始MD5
 *
 * @return
 */
public class RSASignUtil {
    private static Logger logger = LoggerFactory.getLogger(RSASignUtil.class);

    /**
     * 公钥解密
     *
     * @param encryptSign
     * @param publicKey
     * @return
     */
    public static String decryptByPublicKey(String encryptSign, String publicKey) throws Exception {
            logger.info("encryptSign:" + encryptSign);
            logger.info("publicKey:" + publicKey);
            logger.info("签名解密处理开始");
            byte[] signBase64 = Base64.decode(encryptSign);
            String sign = new String(RSAUtil.decryptByPublicKey(signBase64, publicKey));
            logger.info("签名解密处理结束");
            return sign;
    }

    /**
     * 私钥解密
     * @param encryptSign
     * @param privateKey
     * @return
     */
    public static String decryptByPrivateKey(String encryptSign, String privateKey) throws Exception {
            logger.info("encryptSign:" + encryptSign);
            logger.info("privateKey:" + privateKey);
            logger.info("签名解密处理开始");
            byte[] signBase64 = Base64.decode(encryptSign);
            String sign = new String(RSAUtil.decryptByPrivateKey(signBase64, privateKey));
            logger.info("签名解密处理结束");
            return sign;
    }

    /**
     * 私钥加密
     *
     * @param sign
     * @return
     * @throws Exception
     */
    public static String encryptByPrivateKey(String sign, String privateKey) throws Exception {
            byte[] origiSignPublicKeyEncrypt = RSAUtil.encryptByPrivateKey(sign.getBytes(), privateKey);
            byte[] origiSignBase64 = Base64.encode(origiSignPublicKeyEncrypt);
            String encryptSign = new String(origiSignBase64);
            return encryptSign;
    }

    /**
     * 公钥加密
     * @param sign
     * @param publicKey
     * @return
     */
    public static String encryptByPublicKey(String sign, String publicKey) throws Exception {
            byte[] origiSignPublicKeyEncrypt = RSAUtil.encryptByPublicKey(sign.getBytes(), publicKey);
            byte[] origiSignBase64 = Base64.encode(origiSignPublicKeyEncrypt);
            String encryptSign = new String(origiSignBase64);
            return encryptSign;
    }

    public static String getString( Map<String,String> param){
        StringBuilder paramStr = new StringBuilder();

        Map<String, String> paramSort = new TreeMap<>();

        //默认排序，按照ASCII升序
        for (Map.Entry<String, String> entry : param.entrySet()) {
            paramSort.put(entry.getKey(), entry.getValue());
        }

        // 拼接字符串
        for (Map.Entry<String, String> entry : paramSort.entrySet()) {
            paramStr.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        if (paramStr.length() > 0) {
            paramStr.deleteCharAt(0);
        }

        return paramStr.toString();
    }
}
