package com.br.marketing.client.halo;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;


/**
 * -----------------------------
 *
 * @author guangchao.zhang
 * @Date 2022/3/7 10:56 AM
 * -----------------------------
 * @Description halo封装加密
 */
public class EncryptUtil {
    public static void main(String[] args) {
        System.out.println(getMd5Str("15225945555"));
    }

    private static final String MD5 = "MD5";


    public static String signTopRequest(Map<String, String> params, String secret) {
        // 第一步：检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 第二步：把所有参数名和参数值串在一起
        StringBuilder query = new StringBuilder();
        query.append(secret);
        for (String key : keys) {
            if (StringUtils.isNotBlank(key)) {
                Object value = params.get(key);
                if (null != value) {
                    query.append(key.trim());
                    if ("dataItems".equals(key)) {
                        query.append(JSONObject.toJSONString(value).trim());
                    }else {
                        query.append(String.valueOf(value).trim());
                    }



                }
            }
        }
        query.append(secret);
        return getMd5Str(query.toString());
    }

    public static String getStringToSign(JSONObject params, String secret) {
        // 第一步：检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 第二步：把所有参数名和参数值串在一起
        StringBuilder query = new StringBuilder();
        query.append(secret);
        for (String key : keys) {
            if (StringUtils.isNotBlank(key)) {
                Object value = params.get(key);
                if (null != value) {
                    query.append(key.trim());
                    query.append(value.toString().trim());
                }
            }
        }
        query.append(secret);
        return query.toString();
    }

    public static String signTopRequest(JSONObject params, String secret) {
        return getMd5Str(getStringToSign(params, secret));
    }

    /**
     * 获取md5 byte数组
     */
    private static byte[] getMd5(String str) {
        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance(MD5);

            messageDigest.reset();

            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException e) {
            // log.error("NoSuchAlgorithmException caught!");
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        return messageDigest.digest();
    }

    /**
     * MD5 加密
     */
    public static String getMd5Str(String str) {
        return toHex(getMd5(str));
    }

    /**
     * 转小写16进制字符串
     *
     * @param byteArray
     * @return
     */
    private static String toHex(byte[] byteArray) {
        if (null == byteArray) {
            return null;
        }

        StringBuilder md5Str = new StringBuilder();
        for (byte b : byteArray) {
            md5Str.append(String.format("%02x", b));
        }

        return md5Str.toString();
    }
}
