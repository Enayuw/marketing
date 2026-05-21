package com.br.marketing.util.didiai;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 滴滴 AI 接入场景下，将 AES 用密钥源（通常为 {@code dataSecret}，由 {@code didiaiDataSecretMap} 提供）派生为
 * AES-128 固定长度密钥字节。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>将输入字符串按 UTF-8 编码为字节数组；
 *   <li>若长度达到或超过 16 字节，则截取前 16 字节作为 AES-128 密钥；
 *   <li>若长度不足 16 字节，则右侧以二进制 0x00 补足至 16 字节（与 {@link Arrays#copyOf} 语义一致，非字符
 *       {@code '0'}（0x30））。
 * </ul>
 *
 * <p>说明：HMAC-SHA1 验签使用 {@code appSecret}，不经过本类；本工具仅服务 AES-128 密钥材料。
 *
 * @author yueping.bai
 */
public final class DidiaiKeyUtil {

    private static final int AES_128_KEY_LEN = 16;

    private DidiaiKeyUtil() {}

    /**
     * 将密钥源字符串（通常为 dataSecret）派生为长度恒为 16 字节的 AES-128 密钥材料。
     *
     * <p>参数说明：{@code keyMaterial} 为 AES 用全串，允许为 null，此时返回全 0x00 的 16 字节数组。
     *
     * <p>返回值说明：始终返回长度为 16 的字节数组，可直接用于构造 {@link javax.crypto.spec.SecretKeySpec}。
     *
     * @param keyMaterial 密钥源原始字符串，可为 null
     * @return 长度为 16 的 AES 密钥字节数组
     */
    public static byte[] toAes128KeyBytes(String keyMaterial) {
        if (keyMaterial == null) {
            return new byte[AES_128_KEY_LEN];
        }
        byte[] raw = keyMaterial.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(raw, AES_128_KEY_LEN);
    }
}
