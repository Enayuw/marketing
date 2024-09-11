package com.br.marketing.api.customer.upload.service.weiju.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AESUtil {

    public static String decryptBase64Content(String base64Content, String key, String iv) {
        try {
            // 密钥转成⼆进制流
            byte[] secretKeyBytes = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(secretKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
            // base64解码
            byte[] content = Base64.decodeBase64(base64Content);
            // aes解密
            byte[] decryptContent = cipher.doFinal(content);
            return new String(decryptContent, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public static String encryptBase64Content(String plainText, String key, String iv) {
        try {
            byte[] secretKeyBytes = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(secretKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            byte[] encryptedContent = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptedContent);
        } catch (Exception e) {
            return null;
        }
    }

}
