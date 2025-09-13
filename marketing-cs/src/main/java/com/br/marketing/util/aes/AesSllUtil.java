package com.br.marketing.util.aes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AesSllUtil {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String IV = "0102030405060708";


    /**
     * AES 加密操作
     *
     * @param sSrc     明文
     * @param sKey     加密密钥
     * @return 返回HEX加密后的的加密数据
     */
    public static String encrypt(String sSrc, String sKey) {
        try {
            if (sKey == null) {
                //logger.error("Key为空null");
                return null;
            } else if (sKey.length() != 16) {
                //logger.error("Key长度不是16位");
                return null;
            } else {
                byte[] raw = sKey.getBytes();
                SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
                Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
                IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
                cipher.init(1, skeySpec, iv);
                byte[] encrypted = cipher.doFinal(sSrc.getBytes());
                //return (new BASE64Encoder()).encode(encrypted);
                return org.apache.commons.codec.binary.Base64.encodeBase64String(encrypted);

            }
        }catch (Exception e){
            return sSrc;
        }
    }

    /**
     * AES 解密操作
     *
     * @param sSrc    加密后的密文
     * @param sKey     加密密钥
     * @return 明文
     */
    public static String decrypt(String sSrc, String sKey) {
        try {
            if (sKey == null) {
                //logger.error("Key为空null");
                return null;
            } else if (sKey.length() != 16) {
                //logger.error("Key长度不是16位");
                return null;
            } else {
                byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
                SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
                Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
                IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
                cipher.init(2, skeySpec, iv);
                //byte[] encrypted1 = (new BASE64Decoder()).decodeBuffer(sSrc);

                byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(sSrc);
                byte[] output = cipher.doFinal(decoded);
                return new String(output, StandardCharsets.UTF_8);

                //return getString(sSrc, cipher, encrypted1);
            }
        } catch (Exception var10) {
            //logger.error("msf-mq error. decrypt failed.sSrc={}", sSrc, var10);
            return sSrc;
        }
    }

    public static void main(String[] args) {
        //String cell = encrypt("张三", "fC8tzaLDItGjIjOr");
        //System.out.println(cell);

        String decrypt = decrypt("dZQpcDnDS20SrwLj0uAqSQ==", "fC8tzaLDItGjIjOr");
        String decrypt1 = decrypt("BafPuBCgCwhuWfSUYeSk7A==", "fC8tzaLDItGjIjOr");
        System.out.println(decrypt);
        System.out.println(decrypt1);
    }
}
