package com.br.marketing.api.customer.upload.service.weiju.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 微聚AES加密工具
 *
 * @author senyang.zheng
 * @date 2024/09/11
 */
public class AESUtil {
    /**
     * aes加密
     *
     * @param base64Key
     * @param text
     * @return
     */
    public static String encryptAES(String base64Key, String text) {
        try {
            byte[] key = parseHexStr2Byte(base64Key);
            SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(1, sKeySpec);
            byte[] encryptBytes = cipher.doFinal(text.getBytes("utf-8"));
            return parseByte2HexStr(encryptBytes);
        } catch (Exception var6) {
            return null;
        }
    }

    /**
     * aes解密
     *
     * @param base64Key
     * @param text
     */
    public static String decryptAES(String base64Key, String text) {
        try {
            byte[] key = parseHexStr2Byte(base64Key);
            SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, sKeySpec);
            byte[] decryptBytes = cipher.doFinal(parseHexStr2Byte(text));
            return new String(decryptBytes, "UTF-8");
        } catch (Exception var6) {
            return null;
        }
    }

    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte)(high * 16 + low);
            }
            return result;
        }
    }

    public static String parseByte2HexStr(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; ++i) {
            String hex = Integer.toHexString(buf[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
}
