package com.br.marketing.util.aes;

import com.br.marketing.dto.AesGeneralDTO;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 通用AES工具类
 *
 * @author kongbx
 * @date 2025/7/19
 */
public class AesUtil {

    private static final String KEY_ALGORITHM = "AES";

    /**
     * AES加密
     *
     * @param dto 加密参数
     * @return 加密后的Base64字符串
     */
    public static String encryptBase64(AesGeneralDTO dto) {
        try {
            String text = dto.getText();
            String cipherMode = dto.getCipherMode();
            String paddingScheme = dto.getPaddingScheme();
            Charset charset = getCharset(dto.getCharset());
            String dynamicKeys = dto.getDynamicKeys();

            checkKeyLength(dynamicKeys);

            String transformation = KEY_ALGORITHM + "/" + cipherMode + "/" + paddingScheme;
            SecretKeySpec skey = new SecretKeySpec(dynamicKeys.getBytes(charset), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            byte[] crypted = cipher.doFinal(text.getBytes(charset));
            return Base64.encodeBase64String(crypted);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES解密
     *
     * @param dto 解密参数
     * @return 解密后的明文
     */
    public static String decryptBase64(AesGeneralDTO dto) {
        try {
            String text = dto.getText();
            String cipherMode = dto.getCipherMode();
            String paddingScheme = dto.getPaddingScheme();
            Charset charset = getCharset(dto.getCharset());
            String dynamicKeys = dto.getDynamicKeys();

            checkKeyLength(dynamicKeys);

            String transformation = KEY_ALGORITHM + "/" + cipherMode + "/" + paddingScheme;
            SecretKeySpec skey = new SecretKeySpec(dynamicKeys.getBytes(charset), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skey);
            byte[] decoded = Base64.decodeBase64(text);
            byte[] output = cipher.doFinal(decoded);
            return new String(output, charset);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES加密（返回原始二进制数据，不做Base64编码）
     *
     * @param dto 加密参数
     * @return 加密后的字节数组
     */
    public static byte[] encrypt(AesGeneralDTO dto) {
        try {
            String text = dto.getText();
            String cipherMode = dto.getCipherMode();
            String paddingScheme = dto.getPaddingScheme();
            Charset charset = getCharset(dto.getCharset());
            String dynamicKeys = dto.getDynamicKeys();

            checkKeyLength(dynamicKeys);

            String transformation = KEY_ALGORITHM + "/" + cipherMode + "/" + paddingScheme;
            SecretKeySpec skey = new SecretKeySpec(dynamicKeys.getBytes(charset), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            return cipher.doFinal(text.getBytes(charset));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES解密（接收原始二进制数据，不做Base64解码）
     *
     * @param dto 解密参数（除text外其他参数）
     * @return 解密后的明文
     */
    public static String decrypt(AesGeneralDTO dto) {
        try {
            String text = dto.getText();
            String cipherMode = dto.getCipherMode();
            String paddingScheme = dto.getPaddingScheme();
            Charset charset = getCharset(dto.getCharset());
            String dynamicKeys = dto.getDynamicKeys();

            checkKeyLength(dynamicKeys);

            String transformation = KEY_ALGORITHM + "/" + cipherMode + "/" + paddingScheme;
            SecretKeySpec skey = new SecretKeySpec(dynamicKeys.getBytes(charset), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skey);
            byte[] output = cipher.doFinal(text.getBytes(charset));
            return new String(output, charset);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验AES密钥长度
     */
    private static void checkKeyLength(String key) {
        int length = key.getBytes(StandardCharsets.UTF_8).length;
        if (length != 16 && length != 24 && length != 32) {
            throw new IllegalArgumentException("AES密钥长度必须为16/24/32字节，当前为: " + length);
        }
    }

    /**
     * 获取字符集
     */
    private static Charset getCharset(String charset) {
        if (charset == null || charset.isEmpty()) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(charset);
    }
}