package com.br.marketing.client.xiecheng;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Security;
import java.util.Map;

/**
 * 携程金融加密类
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.check.utils
 * @Description: 携程金融加密类
 * @CreateTime: 2022-07-18 16 :33
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public class FinanceAESUtils {
    public static String decryptStr(String str, String aesKey, String aesIv) {
        byte[] decryptRes = Base64Utils.decode(str.getBytes());
        return new String(decrypt(decryptRes, aesKey.getBytes(), aesIv.getBytes()));
    }

    public static String encryptStr(String str, String aesKey, String aesIv) {
        byte[] encode = Base64Utils.encode(encrypt(str.getBytes(), aesKey.getBytes(), aesIv.getBytes()));
        return new String(encode);
    }

    /**
     * AES加密
     * 填充模式AES/CBC/ZeroBytePadding
     * 加密模式128
     *
     * @param originalContent
     * @param encryptKey
     * @param ivByte
     * @return
     */
    private static byte[] encrypt(byte[] originalContent, byte[] encryptKey, byte[] ivByte) {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/ZeroBytePadding");
            SecretKeySpec skeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(ivByte));
            byte[] encrypted = cipher.doFinal(originalContent);
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES解密
     * 填充模式AES/CBC/ZeroBytePadding
     * 解密模式128
     *
     * @param content 目标密文
     * @return
     * @throws Exception
     * @throws
     * @throws
     */
    private static byte[] decrypt(byte[] content, byte[] aesKey, byte[] ivByte) {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/ZeroBytePadding");
            Key sKeySpec = new SecretKeySpec(aesKey, "AES");
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * BouncyCastle作为安全提供，防止我们加密解密时候因为jdk内置的不支持改模式运行报错。
     **/
    private static void initialize() {
        if (initialized) {
            return;
        }
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }

    /**
     * 生成iv
     *
     * @param iv
     * @return
     * @throws Exception
     */
    private static AlgorithmParameters generateIV(byte[] iv) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }

    /**
     * 加签
     *
     * @param signMap
     * @param signKey
     * @return
     */
    public static String signLocal(Map<String, Object> signMap, String signKey) {
        // 排序
        StringBuilder sb = new StringBuilder();
        signMap.keySet().stream().sorted().forEach(p -> sb.append(signMap.get(p)));
        // 拼接signKey
        sb.append(signKey);
        return MD5Util.encode(String.valueOf(sb));
    }


    /**
     * 避免重复new生成多个BouncyCastleProvider对象，因为GC回收不了，会造成内存溢出
     * 只在第一次调用decrypt()方法时才new 对象
     */
    private static boolean initialized = false;

}

