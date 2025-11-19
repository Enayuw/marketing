package com.br.marketing.util.aes;

import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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

    /**
     * 加密 泰康专用
     *
     * @param str jsonString
     * @param key aes key
     * @return 结果
     * @throws Exception
     */
    public static String AesEncrypt(String str, String key) {
        try {
        if (str == null || key == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        byte[] bytes = cipher.doFinal(str.getBytes("utf-8"));
        return new String(Base64.getEncoder().encode(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解密 /Users/zhangchao/IdeaProjects/marketing/marketing-cs/src/main/java/com/br/marketing/util/aes/AesHexUtil.java
     *
     * @param str string
     * @param key aes key
     * @return 结果
     * @throws Exception
     */
    public static String AesDecrypt(String str, String key) throws Exception {
        if (str == null || key == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        byte[] bytes = Base64.getDecoder().decode(str.getBytes());
        bytes = cipher.doFinal(bytes);
        return new String(bytes, "utf-8");
    }

    public static void main(String[] args) {
        String cell = encrypt("13497814301", "nX7zCFT1HaUllNbM");
        String nX7zCFT1HaUllNbM = decrypt("8c1eaa61a12d91865cf47826ad242dbe", "nX7zCFT1HaUllNbM");
        String s = AesEncrypt("13497814301", "nX7zCFT1HaUllNbM");
        System.out.println(cell);
        System.out.println(s);
        System.out.println(nX7zCFT1HaUllNbM);
    }
}
