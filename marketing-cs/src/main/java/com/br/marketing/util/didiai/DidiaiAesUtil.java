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
 *   <li>IV 为定长 16 字节：将毫秒时间戳的十进制字符串作字符级反转，再经 UTF-8 编码，拷贝入 16 字节缓冲左侧，剩余字节为
 *       0x00（与对端/客户 {@code generateIv} 一致；禁止使用「在字符串侧补字符 '0' 再取 UTF-8」的旧规则）；</li>
 *   <li>密钥为 16 字节 AES 材料，由 {@link DidiaiKeyUtil#toAes128KeyBytes(String)} 从 dataSecret 派生；</li>
 *   <li>密文在网络中常以 Base64 文本传输，解密时入参为 Base64 字符串，内部先解码再执行 CBC 解密。
 * </ul>
 *
 * <p>编码约定：明文字节为 UTF-8。
 *
 * @author yueping.bai
 */
public final class DidiaiAesUtil {

    private static final String AES = "AES";
    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final int IV_LEN = 16;

    private DidiaiAesUtil() {}

    /**
     * 由毫秒时间戳派生 16 字节 IV，与对端/客户对 {@code timestamp} 字符串反序后 UTF-8 再 0x00 填充一致。
     *
     * <p>参数说明：与请求头中 {@code timestamp} 的数值在十进制表示下相同（如 {@code 1777282809463L} 对应字符串
     * {@code "1777282809463"}，再作 {@link StringBuilder#reverse()}）。
     *
     * <p>返回值说明：长度恒为 16 的 IV 原始字节，可直接用于 {@link IvParameterSpec}。
     *
     * @param timestampMillis 毫秒时间戳
     * @return 定长 16 字节 IV
     */
    public static byte[] genIvBytes(long timestampMillis) {
        String reversed = new StringBuilder(Long.toString(timestampMillis)).reverse().toString();
        byte[] b = reversed.getBytes(StandardCharsets.UTF_8);
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(b, 0, iv, 0, Math.min(b.length, IV_LEN));
        return iv;
    }

    /**
     * 将 Base64 密文解密为 UTF-8 明文字符串。
     *
     * <p>参数说明：{@code encryptedBase64} 为 Base64 文本；{@code keyBytes} 为 16 字节 AES 密钥；{@code ivBytes} 为 16
     * 字节 IV。
     *
     * <p>返回值说明：解密后的业务 JSON 或其它明文字符串。
     *
     * <p>异常说明：当 Base64 非法、密钥或 IV 长度与算法要求不符、或密文被篡改导致填充错误时，由底层 {@link Cipher} 抛出异常，
     * 由调用方转换为业务错误码。
     *
     * @param encryptedBase64 Base64 编码的密文
     * @param keyBytes        16 字节 AES 密钥
     * @param ivBytes         16 字节 IV
     * @return UTF-8 明文
     * @throws Exception 解密过程中任一环节失败时抛出
     */
    public static String decrypt(String encryptedBase64, byte[] keyBytes, byte[] ivBytes)
            throws Exception {
        byte[] cipherBytes = Base64.decodeBase64(encryptedBase64.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
        byte[] plain = cipher.doFinal(cipherBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    /**
     * 将 UTF-8 明文加密为 Base64 密文，便于与对端联调或编写本地加密用例。
     *
     * <p>参数说明：{@code plainUtf8} 为待加密明文；{@code keyBytes} 与 {@code ivBytes} 含义同 {@link #decrypt}。
     *
     * <p>返回值说明：Base64 编码后的密文字符串。
     *
     * <p>异常说明：加密失败时由底层抛出 {@link Exception}，由调用方处理。
     *
     * @param plainUtf8 明文
     * @param keyBytes  16 字节 AES 密钥
     * @param ivBytes   16 字节 IV
     * @return Base64 密文
     * @throws Exception 加密失败时抛出
     */
    public static String encrypt(String plainUtf8, byte[] keyBytes, byte[] ivBytes) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
        byte[] encrypted = cipher.doFinal(plainUtf8.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encrypted);
    }
}
