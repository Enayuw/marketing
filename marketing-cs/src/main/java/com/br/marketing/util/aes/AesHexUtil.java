package com.br.marketing.util.aes;

import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesHexUtil {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";// 默认的加密算法

    /**
     * AES 加密操作
     *
     * @param content 明文
     * @param key     加密密钥
     * @return 返回HEX加密后的的加密数据
     */
    public static String encrypt(String content, String key) {
        try {
            // 创建密码器
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            byte[] byteContent = content.getBytes("utf-8");
            // 初始化为加密模式的密码器
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), KEY_ALGORITHM));
            // 加密
            byte[] result = cipher.doFinal(byteContent);
            // 通过Base64转码返回
            return new String(Hex.encode(result));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES 解密操作
     *
     * @param content 经HEX加密后的密文
     * @param key     加密密钥
     * @return 明文
     */
    public static String decrypt(String content, String key) {
        try {
            // 实例化
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            // 使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), KEY_ALGORITHM));

            // 执行操作
            byte[] result = cipher.doFinal(Hex.decode(content));

            return new String(result, "utf-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        String cell = encrypt("13497814301", "Vje1kFHChlm8khlc");
//        System.out.println(cell);
    }
}
