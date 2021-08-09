package com.br.marketing.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * des加解密
 *
 * @Author yunfei.dong
 * @CreateDate 2019/7/10 11:49
 * @UpdateUser linquan.guo
 * @UpdateDate 2019/7/10 11:49
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class ThreeDes {

    private static final String DES = "DES";
    private static final String ECB_PADDING = "DESede/ECB/PKCS5Padding";
    private static final String CBC_PADDING = "DES/CBC/PKCS5Padding";
    private static final Pattern R_PATTERN  = Pattern.compile("\r");
    private static final Pattern N_PATTERN  = Pattern.compile("\n");

    /**
     * @param src  明文
     * @param key  3DES ECB加密,key必须是长度大于等于24 位
     */
    public static String encryptByECB(final String src, final String key) throws Exception {
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);

        final Cipher cipher = Cipher.getInstance(ECB_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, securekey);
        final byte[] b = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
        final Base64.Encoder encoder = Base64.getEncoder();
        String rStr = R_PATTERN.matcher(encoder.encodeToString(b)).replaceAll("");
        return N_PATTERN.matcher(rStr).replaceAll("");
    }

    public static String decryptByECB(final String src, final String key) throws Exception {
        // --通过base64,将字符串转成byte数组
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] bytesrc = decoder.decode(src);
        // --解密的key
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);
        // --Chipher对象解密
        final Cipher cipher = Cipher.getInstance(ECB_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, securekey);
        final byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte, StandardCharsets.UTF_8);
    }

    public static String encryptByCBC(final String src, final String key ,String iV) throws Exception {
        // --生成key,同时制定是des还是DESede,两者的key长度要求不同
        final DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        final SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        // --加密向量
        final IvParameterSpec iv = new IvParameterSpec(iV.getBytes(StandardCharsets.UTF_8));
        // --通过Chipher执行加密得到的是一个byte的数组,Cipher.getInstance("DES")就是采用ECB模式,cipher.init(Cipher.ENCRYPT_MODE,
        // secretKey)就可以了.
        final Cipher cipher = Cipher.getInstance(CBC_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        final byte[] b = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
        // --通过base64,将加密数组转换成字符串
        final Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(b);
    }


    public static  String decryptByCBC(final String src, final String key,String iV) throws Exception {
        // --通过base64,将字符串转成byte数组
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] bytesrc = decoder.decode(src);
        // --解密的key
        final DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        final SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        // --向量
        final IvParameterSpec iv = new IvParameterSpec(iV.getBytes(StandardCharsets.UTF_8));
        final Cipher cipher = Cipher.getInstance(CBC_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        final byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            String content = "张三";
            String key = "1234567890asdfghjklzxcvbnm";
            String encData = encryptByECB(content, key);
            log.warn("密文 ：{} ", encData);
            String decData = decryptByECB(encData, key);
            log.warn("明文 ：{} ", decData);
        } catch (Exception e) {
            log.error("解密失败", e);
        }
    }

}