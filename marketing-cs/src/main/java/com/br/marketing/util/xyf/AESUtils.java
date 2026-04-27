package com.br.marketing.util.xyf;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class AESUtils {

    /**
     * AES
     **/
    public static final String KEY_ALGORITHM = "AES";
    public static final String ENCODING = "utf-8";

    /**
     * 获取AES密钥
     *
     * @return
     */
    public static String generateAESKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        byte[] keyExternal = key.getEncoded();
        return Base64.encodeBase64String(keyExternal);
    }

    /**
     * AES加密
     *
     * @param content
     * @param key
     * @return
     */
    public static String encrypt(String content, String key, Boolean decodeBase64) {
        try {
            byte[] bytesKey;
            if (decodeBase64) {
                bytesKey = Base64.decodeBase64(key.getBytes(StandardCharsets.UTF_8));
            } else {
                bytesKey = key.getBytes();
            }
            SecretKeySpec secretKey = new SecretKeySpec(bytesKey, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);// 创建密码器
            byte[] byteContent = content.getBytes(ENCODING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);// 初始化
            byte[] result = cipher.doFinal(byteContent);// 加密
            return Base64.encodeBase64String(result);
        } catch (Exception e) {
            // 加密异常

        }
        return null;
    }

    /**
     * AES解密
     *
     * @param content
     * @param key
     * @return
     */
    public static String decrypt(String content, String key, Boolean decodeBase64) {
        try {
            byte[] bytesKey;
            if (decodeBase64) {
                bytesKey = Base64.decodeBase64(key.getBytes(StandardCharsets.UTF_8));
            } else {
                bytesKey = key.getBytes();
            }
            SecretKeySpec secretKey = new SecretKeySpec(bytesKey, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);// 创建密码器

            cipher.init(Cipher.DECRYPT_MODE, secretKey);// 初始化
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));// 解密
            return new String(result);
        } catch (Exception e) {
            // 解密异常

        }
        return null;
    }
}
