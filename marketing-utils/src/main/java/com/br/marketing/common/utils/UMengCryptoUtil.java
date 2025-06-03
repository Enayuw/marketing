package com.br.marketing.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 友盟参数加解密-sign生成工具
 */
@Slf4j
public class UMengCryptoUtil {

    /**
     * 请求友盟侧-请求body参数加密
     * @param secret
     * @param requestBody
     * @return
     */
    public static String encryptBody(String secret, String requestBody) {
        byte[] postBodyBytes = requestBody.getBytes();
        byte[] secretBytes = secret.getBytes();
        byte[] result = new byte[postBodyBytes.length];
        for (int i = 0; i < postBodyBytes.length; i++) {
            result[i] = (byte) (postBodyBytes[i] ^ secretBytes[i % secretBytes.length]);
        }
        return Base64.getUrlEncoder().encodeToString(result);
    }


    /**
     * 对友盟侧的请求-body进行解密
     * @param secret
     * @param requestBody
     * @return
     */
    public static String decryptBody(String secret, String requestBody) {
        byte[] decryptBody = Base64.getUrlDecoder().decode(requestBody);
        byte[] secretBytes = secret.getBytes();
        byte[] result = new byte[decryptBody.length];
        for (int i = 0; i < decryptBody.length; i++) {
            result[i] = (byte) (decryptBody[i] ^ secretBytes[i % secretBytes.length]);
        }
        return new String(result, StandardCharsets.UTF_8);
    }


    /**
     * 请求友盟侧-请求参数sign生成
     * @param bizId
     * @param bizSecret
     * @param postBody
     * @param rid
     * @return sign
     */
    public static String getRequestSign(String bizId, String bizSecret, String postBody, String rid) {
        String sign = "";
        try {
            byte[] bizIdBytes = bizId.getBytes(StandardCharsets.UTF_8);
            byte[] postBodyBytes = postBody.getBytes(StandardCharsets.UTF_8);
            byte[] bizSecretBytes = bizSecret.getBytes(StandardCharsets.UTF_8);
            byte[] ridBytes = rid.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(bizIdBytes.length + postBodyBytes.length + bizSecretBytes.length + ridBytes.length);
            buffer.put(bizIdBytes);
            buffer.put(postBodyBytes);
            buffer.put(bizSecretBytes);
            buffer.put(ridBytes);
            sign = Hex.encodeHexString(MessageDigest.getInstance("md5").digest(buffer.array()));
        }catch (Exception e) {
            log.error("uMeng sign error", e);
        }
        return sign;
    }
}
