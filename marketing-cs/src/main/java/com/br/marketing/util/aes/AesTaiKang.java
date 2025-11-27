package com.br.marketing.util.aes;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * -----------------------------
 * PackageName： com.br.marketing.util.aes
 * ClassName：AesTaiKang
 * Description：
 *
 * @author：it-yml CreateTime：2025-11-19
 * -----------------------------
 */
public class AesTaiKang {
    /**
     * 加密
     *
     * @param str jsonString
     * @param key aes key
     * @return 结果
     * @throws Exception
     */
    public static String AesEncrypt(String str, String key)  {
        try {
            if (str == null || key == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
            byte[] bytes = cipher.doFinal(str.getBytes("utf-8"));
            return new String(Base64.getEncoder().encode(bytes));
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 解密
     *
     * @param str string
     * @param key aes key
     * @return 结果
     * @throws Exception
     */
    public static String AesDecrypt(String str, String key)  {
        try {
            if (str == null || key == null){
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
            byte[] bytes = Base64.getDecoder().decode(str.getBytes());
            bytes = cipher.doFinal(bytes);
            return new String(bytes, "utf-8");
        }catch (Exception e){
            return null;
        }
    }
}
