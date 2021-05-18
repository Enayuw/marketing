package com.br.marketing.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

/**
 * @author br
 */
public class ThreeDes {

    private static String DES = "DES";
    private static String DESede = "DESede";
    private static String ECB_PADDING = "DESede/ECB/PKCS5Padding";
    private static String CBC_PADDING = "DES/CBC/PKCS5Padding";



    /**
     * @param src  明文
     * @param key  3DES ECB加密,key必须是长度大于等于24 位
     */
    public static String encryptByEcb(final String src, final String key) throws Exception {
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DESede);
        final SecretKey securekey = keyFactory.generateSecret(dks);

        final Cipher cipher = Cipher.getInstance(ECB_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, securekey);
        final byte[] b = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(b).replaceAll("\r", "").replaceAll("\n", "");
    }

    public static String decryptByEcb(final String src, final String key) throws Exception {
        // --通过base64,将字符串转成byte数组
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(src);
        // --解密的key
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DESede);
        final SecretKey securekey = keyFactory.generateSecret(dks);
        // --Chipher对象解密
        final Cipher cipher = Cipher.getInstance(ECB_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, securekey);
        final byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte,StandardCharsets.UTF_8);
    }

    public static String encryptByCbc(final String src, final String key , String iV) throws Exception {
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
        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(b);
    }


    public static String decryptByCbc(final String src, final String key, String iV) throws Exception {
        // --通过base64,将字符串转成byte数组
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(src);
        // --解密的key
        final DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        final SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        // --向量
        final IvParameterSpec iv = new IvParameterSpec(iV.getBytes(StandardCharsets.UTF_8));
        final Cipher cipher = Cipher.getInstance(CBC_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        final byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte,StandardCharsets.UTF_8);

    }
}