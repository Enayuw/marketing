package com.br.marketing.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AESUtil {
    private SecretKey sSecretKey = null;
    private Cipher sCipher = null;
    private String sKeyString = null;
    private final static String zeroByPadding = "AES/CBC/ZeroBytePadding";
    private final static String key_algorithm = "AES";


 /**
     * AES加密
     *
     * @param log
     * @param key 秘钥
     * @return
     */
    public static String aesEncrypty(String log,String key) {
        return encrypt(log,key);
    }

    /**
     * aes解密
     *
     * @param log
     * @param key 秘钥
     * @return
     */
    public static String aesDecrypt(String log,String key) {
        return decrypt(log,key);
	}



    public static String genKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, new SecureRandom());
            SecretKey aesKey = keyGen.generateKey();
            return new String(aesKey.getEncoded(), "ISO-8859-1");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException var2) {
            return "";
        }
    }

    public static Cipher getKey(String key, int model) {
        try {
            return getKey(key.getBytes("ISO-8859-1"), model);
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static Cipher getKey(byte[] keyBytes, int model) {
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(keyBytes);
        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(model, skeySpec, iv);
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException var6) {
            var6.printStackTrace();
        }

        return cipher;
    }

    public static String encrypt(Cipher cipher, String value) {
        try {
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(encrypted, "ISO-8859-1");
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static String decrypt(Cipher cipher, String encrypted) {
        try {
            return decrypt(cipher, encrypted.getBytes("ISO-8859-1"));
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static String encrypt(String input, String key) {
        byte[] crypted = null;

        try {
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, skey);
            crypted = cipher.doFinal(input.getBytes());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return new String(Base64.encodeBase64(crypted));
    }

    public static String decrypt(String input, String key) {
        byte[] output = null;

        try {
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, skey);
            output = cipher.doFinal(Base64.decodeBase64(input));
        } catch (Exception var5) {
            var5.printStackTrace();
        }
        if(output==null){
            return "";
        }else{
            return new String(output);
        }
    }

    public static String decrypt(Cipher cipher, byte[] encryptedBytes) {
        try {
            byte[] original = cipher.doFinal(encryptedBytes);
            return new String(original);
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public AESUtil(String sKeyString) {
        try {
            this.sSecretKey = new SecretKeySpec(sKeyString.getBytes("UTF-8"), "AES");
            this.sCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public String encryptByECB(String message) {
        String result = "";
        String newResult = "";

        try {
            this.sCipher.init(1, this.sSecretKey);
            byte[] resultBytes = this.sCipher.doFinal(message.getBytes("UTF-8"));
            result = new String(Base64.encodeBase64URLSafeString(resultBytes));
            newResult = this.filter(result);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return newResult;
    }

    public String decryptByECB(String message) {
        String result = "";

        try {
            byte[] messageBytes = Base64.decodeBase64(message);
            this.sCipher.init(2, this.sSecretKey);
            byte[] resultBytes = this.sCipher.doFinal(messageBytes);
            result = new String(resultBytes, "UTF-8");
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return result;
    }

    public String filter(String str) {
        String output = "";
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < str.length(); ++i) {
            int asc = str.charAt(i);
            if (asc != '\n' && asc != '\r') {
                sb.append(str.subSequence(i, i + 1));
            }
        }

        output = new String(sb);
        return output;
    }

    public static String encryptIv(String key,String value,String iv,String charset){
        byte[] encrypt = encrypt(key.getBytes(), value, iv, charset);
        return new String(Base64.encodeBase64(encrypt));
    }

    private static byte[] encrypt(byte[] rawKey, String clearPwd,String iv,String charset) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, key_algorithm);
            Cipher cipher = Cipher.getInstance(zeroByPadding);
            IvParameterSpec _iv = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,_iv);
            byte[] encypted = cipher.doFinal(clearPwd.getBytes(charset));
            return encypted;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
