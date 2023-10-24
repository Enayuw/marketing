package com.br.marketing.client.qifu.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @Author: wangshaowen
 * @Date: 2021/11/22 14:19
 * @Description:
 */
public class RSAUtil {
    /**
     * 加密算法
     */
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";
    /**
     * 默认字符集编码
     */
    private static final String ENCODING = "UTF-8";
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    /**
     * 随机生成RSA公私钥
     *
     * @return
     */
    public static Map<String, String> generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // 获取RSA公私钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            // 转换成字符串
            String publicKeyStr = Base64.encodeBase64String(publicKey.getEncoded());
            String privateKeyStr = Base64.encodeBase64String(privateKey.getEncoded());

            Map<String, String> keyMap = new HashMap<>();
            keyMap.put(PUBLIC_KEY, publicKeyStr);
            keyMap.put(PRIVATE_KEY, privateKeyStr);
            return keyMap;
        } catch (Exception e) {
            throw new RuntimeException("RSA generate key pair error!", e);
        }
    }

    /**
     * 使用公钥加密
     *
     * @param key
     * @param data
     * @return
     */
    public static String encryptByPublicKey(String key, String data) {
        try {
            PublicKey publicKey = getPublicKey(key);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] bs = cipher.doFinal(data.getBytes(ENCODING));
            return new String(Base64.encodeBase64(bs), ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("RSA encrypt error!", e);
        }
    }

    /**
     * 使用私钥解密
     *
     * @param key
     * @param data
     * @return
     */
    public static String decryptByPrivateKey(String key, String data) {
        try {
            PrivateKey privateKey = getPrivateKey(key);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bs = cipher.doFinal(Base64.decodeBase64(data.getBytes(ENCODING)));
            return new String(bs, ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("RSA decrypt error!", e);
        }
    }

    public static String generateContent(Map<String, Object> params) {
        Map<String, Object> kvMap = new TreeMap<>();
        params.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!"sign".equalsIgnoreCase(key)
                    && !"signType".equalsIgnoreCase(key)
                    && Objects.nonNull(value)
                    && StringUtils.isNotBlank(value.toString())) {
                kvMap.put(key, value);
            }
        });
        return Joiner.on("&").withKeyValueSeparator("=").join(kvMap);
    }

    public static String generateContent(JSONObject jsonObject) {
        Assert.notNull(jsonObject, "内容不可为null");
        Map<String, Object> kvMap = new TreeMap<>();
        jsonObject.forEach((k, v) -> {
            if (!"sign".equalsIgnoreCase(k)
                    && !"signType".equalsIgnoreCase(k)
                    && Objects.nonNull(v)
                    && StringUtils.isNotBlank(v.toString())) {
                kvMap.put(k, v);
            }
        });
        return Joiner.on("&").withKeyValueSeparator("=").join(kvMap);
    }

    /**
     * 使用私钥签名
     *
     * @param key
     * @param data
     * @return
     */
    public static String signByPrivateKey(String key, String data) {
        try {
            PrivateKey privateKey = getPrivateKey(key);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(ENCODING));
            byte[] bs = signature.sign();
            return new String(Base64.encodeBase64(bs), ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("RSA sign error!", e);
        }
    }

    /**
     * 使用公钥验签
     *
     * @param key
     * @param originSign
     * @param sign
     * @return
     */
    public static boolean verifySignByPublicKey(String key, String originSign, String sign) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            byte[] bs = Base64.decodeBase64(key.getBytes(ENCODING));
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(bs));
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(sign.getBytes(ENCODING));
            return signature.verify(Base64.decodeBase64(originSign.getBytes(ENCODING)));
        } catch (Exception e) {
            throw new RuntimeException("RSA verify sign error!", e);
        }
    }

    /**
     * 获取公钥
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 获取私钥
     *
     * @param key
     * @return
     */
    private static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] bs = Base64.decodeBase64(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bs);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }
}