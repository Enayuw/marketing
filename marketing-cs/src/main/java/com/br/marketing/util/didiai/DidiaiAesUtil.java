package com.br.marketing.util.didiai;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 滴滴 AI 接入协议中的 AES-128-CBC 对称加解密工具，填充方式为 PKCS5Padding。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>IV 由毫秒时间戳派生：先将时间戳转为十进制字符串，再整体字符顺序反转，若长度不足 16 个字符则在右侧补
 *       字符 0 直至总长为 16；若反转后超过 16 个字符则截取前 16 个字符；
 *   <li>密钥为 16 字节 AES 密钥，通常由 DidiaiKeyUtil 从 appSecret 派生；
 *   <li>密文在网络中常以 Base64 文本传输，解密时入参为 Base64 字符串，内部先解码再执行 CBC 解密。
 * </ul>
 *
 * <p>编码约定：明文与 IV 字符串均按 UTF-8 转为字节后参与加解密。
 *
 * @author yueping.bai
 */
public final class DidiaiAesUtil {

    private static final String AES = "AES";
    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final int IV_LEN = 16;

    private DidiaiAesUtil() {}

    /**
     * 根据毫秒时间戳生成长度为 16 的 IV 字符串，用于 AES-CBC 模式下的 IvParameterSpec。
     *
     * <p>参数说明：timestampMillis 为与请求头一致的毫秒级 Unix 时间戳。
     *
     * <p>返回值说明：长度恒为 16 的字符串，与对端约定算法一致时可直接用于加解密两侧。
     *
     * @param timestampMillis 毫秒时间戳
     * @return 16 字符长度的 IV 字符串
     */
    public static String genIv(long timestampMillis) {
        String reversed = new StringBuilder(Long.toString(timestampMillis)).reverse().toString();
        if (reversed.length() >= IV_LEN) {
            return reversed.substring(0, IV_LEN);
        }
        StringBuilder sb = new StringBuilder(reversed);
        while (sb.length() < IV_LEN) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * 将 Base64 密文解密为 UTF-8 明文字符串。
     *
     * <p>参数说明：encryptedBase64 为 Base64 文本；keyBytes 为 16 字节 AES 密钥；iv 为 16 字符 IV 字符串。
     *
     * <p>返回值说明：解密后的业务 JSON 或其它明文字符串。
     *
     * <p>异常说明：当 Base64 非法、密钥长度不是 16、或密文被篡改导致填充错误时，由底层 Cipher 抛出异常，
     * 此处不吞掉异常，由调用方转换为业务错误码。
     *
     * @param encryptedBase64 Base64 编码的密文
     * @param keyBytes        16 字节 AES 密钥
     * @param iv              16 字符 IV
     * @return UTF-8 明文
     * @throws Exception 解密过程中任一环节失败时抛出
     */
    public static String decrypt(String encryptedBase64, byte[] keyBytes, String iv) throws Exception {
        byte[] cipherBytes = Base64.decodeBase64(encryptedBase64.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
        byte[] plain = cipher.doFinal(cipherBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    /**
     * 将 UTF-8 明文加密为 Base64 密文，便于与对端联调或编写本地加密用例。
     *
     * <p>参数说明：plainUtf8 为待加密明文；keyBytes 与 iv 含义同 decrypt 方法。
     *
     * <p>返回值说明：Base64 编码后的密文字符串。
     *
     * <p>异常说明：加密失败时抛出 Exception，由调用方处理。
     *
     * @param plainUtf8 明文
     * @param keyBytes  16 字节 AES 密钥
     * @param iv        16 字符 IV
     * @return Base64 密文
     * @throws Exception 加密失败时抛出
     */
    public static String encrypt(String plainUtf8, byte[] keyBytes, String iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
        byte[] encrypted = cipher.doFinal(plainUtf8.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encrypted);
    }
}
