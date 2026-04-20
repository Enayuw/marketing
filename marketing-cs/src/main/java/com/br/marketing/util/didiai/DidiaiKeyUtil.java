package com.br.marketing.util.didiai;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 滴滴 AI 接入场景下，将配置中的应用密钥 appSecret 派生为 AES-128 算法所需的固定长度密钥字节。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>将字符串按 UTF-8 编码为字节数组；
 *   <li>若长度达到或超过 16 字节，则截取前 16 字节作为 AES-128 密钥；
 *   <li>若长度不足 16 字节，则在右侧以数值 0 的字节补足至 16 字节（与 Arrays.copyOf 语义一致）。
 * </ul>
 *
 * <p>说明：HMAC-SHA1 验签仍使用原始 appSecret 字符串的字节序列，本工具仅服务于 AES 密钥长度约束。
 *
 * @author yueping.bai
 */
public final class DidiaiKeyUtil {

    private static final int AES_128_KEY_LEN = 16;

    private DidiaiKeyUtil() {}

    /**
     * 将 appSecret 派生为长度恒为 16 字节的 AES-128 密钥材料。
     *
     * <p>参数说明：appSecret 为配置或密钥表中的应用密钥，允许为 null，此时返回全零 16 字节数组。
     *
     * <p>返回值说明：始终返回长度为 16 的字节数组，可直接用于构造 AES SecretKeySpec。
     *
     * @param appSecret 应用密钥原始字符串，可为 null
     * @return 长度为 16 的 AES 密钥字节数组
     */
    public static byte[] toAes128KeyBytes(String appSecret) {
        if (appSecret == null) {
            return new byte[AES_128_KEY_LEN];
        }
        byte[] raw = appSecret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= AES_128_KEY_LEN) {
            return Arrays.copyOf(raw, AES_128_KEY_LEN);
        }
        return Arrays.copyOf(raw, AES_128_KEY_LEN);
    }
}
