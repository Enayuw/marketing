package com.br.marketing.api.customer.upload.service.weiju.util;

import java.util.Base64;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class RSAEncryptUtil {

    /**
     * 加签
     *
     * @param signParams
     * @param privateKey
     * @return
     */
    public static String signSHA1(Map<String, String> signParams, String privateKey) {
        return initSign("SHA1withRSA", signParams, privateKey);
    }

    /**
     * 验签
     *
     * @param signParams
     * @param sign
     * @param publicKey
     * @return
     */
    public static boolean checkSignSHA1(Map<String, String> signParams, String sign, String publicKey) {
        return rsa256CheckContent("SHA1withRSA", signParams, sign, publicKey);
    }

    private static boolean rsa256CheckContent(String signType, Map<String, String> signParams, String sign, String publicKey) {
        try {
            String content = getSignContent(signParams);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.getDecoder().decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature signature = Signature.getInstance(signType);
            signature.initVerify(pubKey);
            signature.update(content.getBytes("utf-8"));
            boolean bverify = signature.verify(Base64.getDecoder().decode(sign));
            return bverify;
        } catch (Exception var10) {
            var10.printStackTrace();
            return false;
        }
    }

    private static String initSign(String signType, Map<String, String> signParams, String privateKey) {
        try {
            String content = getSignContent(signParams);
            PrivateKey priKey = getPrivateKeyFromPKCS8(privateKey);
            Signature signature = Signature.getInstance(signType);
            signature.initSign(priKey);
            signature.update(content.getBytes("utf-8"));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception var7) {
            System.out.println("生成签名异常:" + var7.getMessage());
            var7.printStackTrace();
            return null;
        }
    }

    public static String getSignContent(Map<String, String> sortedParams) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList(sortedParams.keySet());
        // 对key进行排序
        Collections.sort(keys);
        int index = 0;
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            String value = sortedParams.get(key);
            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank((value))) {
                content.append((index == 0 ? "" : "&") + key + "=" + value);
            }
        }
        return content.toString();
    }

    private static PrivateKey getPrivateKeyFromPKCS8(String priKey) {
        PrivateKey privateKey = null;
        if (StringUtils.isBlank(priKey)) {
            return privateKey;
        } else {
            try {
                PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priKey));
                KeyFactory keyf = KeyFactory.getInstance("RSA");
                privateKey = keyf.generatePrivate(priPKCS8);
            } catch (Exception var4) {
                System.out.println("私钥解析错误:" + var4.getMessage());
            }
            return privateKey;
        }
    }
}
