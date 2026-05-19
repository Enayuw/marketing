package com.br.marketing.util.didiai;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 滴滴 AI 接入协议中的 HMAC-SHA1 签名计算与验签工具。
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>签名原文 signData 由三部分按顺序直接拼接而成：业务明文字符串 params、应用标识 appKey、毫秒时间戳
 *       timestamp 的十进制字符串形式，中间无任何分隔符；
 *   <li>签名值 sign 为对 signData 使用 UTF-8 字节做 HMAC-SHA1，再以 Base64 编码得到的字符串；
 *   <li>验签时使用与对端完全相同的 params、appKey、timestamp 及 appSecret，重新计算后与请求头中的 sign
 *       比较；比较 HMAC 原始字节时使用 MessageDigest.isEqual，避免短字符串比较带来的时序风险。
 * </ul>
 *
 * <p>注意：params 必须与对端参与签名的明文字节序列完全一致（含 JSON 空格、键顺序、转义等），否则验签失败。
 *
 * @author yueping.bai
 */
public final class DidiaiSignUtil {

    private static final String HMAC_SHA1 = "HmacSHA1";

    private DidiaiSignUtil() {}

    /**
     * 根据滴滴协议计算 Base64 格式的 HMAC-SHA1 签名，可用于联调脚本或服务端自测。
     *
     * <p>参数说明：
     *
     * <ul>
     *   <li>params：解密后或与对端约定一致的业务 JSON 明文字符串；
     *   <li>appKey：请求头中的应用标识；
     *   <li>timestamp：毫秒时间戳，拼接时使用 Java 中 long 的十进制字符串形式，与请求头解析后的数值一致；
     *   <li>appSecret：与 appKey 配对的密钥，用于初始化 Mac。
     * </ul>
     *
     * <p>返回值说明：Base64 编码的签名字符串，可直接放入请求头 sign 字段进行对比。
     *
     * <p>异常说明：当 JVM 不支持 HmacSHA1 算法或密钥材料非法时抛出对应受检异常。
     *
     * @param params    业务明文字符串
     * @param appKey    应用标识
     * @param timestamp 毫秒时间戳
     * @param appSecret 应用密钥
     * @return Base64 编码的 HMAC-SHA1 结果
     * @throws NoSuchAlgorithmException 当前环境不支持 HmacSHA1 算法时抛出
     * @throws InvalidKeyException      appSecret 无法用于构造 Mac 密钥时抛出
     */
    public static String sign(String params, String appKey, long timestamp, String appSecret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String signData = params + appKey + timestamp;
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
        byte[] raw = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(raw);
    }

    /**
     * 校验请求头中的签名是否与本地根据明文及密钥重算的结果一致。
     *
     * <p>参数说明：sign 为请求头传入的 Base64 字符串；其余参数与 sign 方法含义相同，且 timestamp 须与
     * 请求头中解析出的毫秒时间戳数值一致。
     *
     * <p>返回值说明：验签通过返回 true；任一参数不合法、Base64 解码失败、或计算结果不一致时返回 false，
     * 不向外抛出受检异常，便于接入层统一返回业务错误码。
     *
     * @param params    解密后的业务明文字符串
     * @param appKey    应用标识
     * @param timestamp 毫秒时间戳
     * @param appSecret 应用密钥
     * @param sign      请求头中的 Base64 签名
     * @return 验签是否通过
     */
    public static boolean verify(
            String params, String appKey, long timestamp, String appSecret, String sign) {
        if (sign == null || appSecret == null) {
            return false;
        }
        try {
            String signData = params + appKey + timestamp;
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
            byte[] expected = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
            byte[] actual = Base64.decodeBase64(sign.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }
}
