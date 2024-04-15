package com.br.marketing.client.qifu.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Author: wangshaowen
 * @Date: 2021/11/22 14:19
 * @Description:
 */
public class AESUtil {

    /**
     * 生成AES加密所需key的算法
     */
    private static final String KEY_ALGORITHM = "AES";
    /**
     * 默认字符集编码
     */
    private static final String ENCODING = "UTF-8";
    /**
     * 默认的加解密算法(加解密算法/工作模式/填充方式)
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 加密
     *
     * @param key  加密的KEY
     * @param iv   加密的IV
     * @param data 解密的数据
     * @return
     */
    public static String encrypt(String key, String iv, String data) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(ENCODING));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(ENCODING), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return com.br.marketing.client.qifu.util.StringUtil.bytes2HexStr(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt error!", e);
        }
    }

    /**
     * 解密
     *
     * @param key  解密的KEY
     * @param iv   解密的IV
     * @param data 解密的数据
     * @return
     */
    public static String decrypt(String key, String iv, String data) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(ENCODING));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(ENCODING), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] original = cipher.doFinal(com.br.marketing.client.qifu.util.StringUtil.hexStr2Bytes(data));
            return new String(original, ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt error!", e);
        }
    }
}