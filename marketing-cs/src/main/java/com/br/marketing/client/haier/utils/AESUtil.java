package com.br.marketing.client.haier.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * AES 加解密工具
 *
 * @author senyang.zheng
 * @date 2023/12/23
 */
public class AESUtil {
    /**
     * 加密
     *
     * @param content  明文
     * @param password 密码
     * @return {@link String }
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2023/12/23
     */
    public static String encrypt(String content, String password) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes("UTF-8"));
        keyGenerator.init(128,secureRandom);
        Key secretKey = keyGenerator.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] byteContent = content.getBytes("utf-8");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] result = cipher.doFinal(byteContent);
        return Base64.encodeBase64String(result);
    }


    /**
     * 解密
     *
     * @param content  密文
     * @param password 密码
     * @return {@link String }
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2023/12/23
     */
    public static String decrypt(String content, String password) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes("UTF-8"));
        keyGenerator.init(128,secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] encoded = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(encoded, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] result = cipher.doFinal(Base64.decodeBase64(content));
        return new String(result);
    }
}
