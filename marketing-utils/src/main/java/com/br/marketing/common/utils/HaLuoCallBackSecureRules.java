package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 友盟参数加解密-sign生成工具
 */
/**
 * 哈啰-api安全规范
 * 需求:https://c.100credit.cn/pages/viewpage.action?pageId=223429538
 */
@Slf4j
public class HaLuoCallBackSecureRules {

    public static final String K_SIGN = "sign";

    public static void signTopRequest(JSONObject params, String secret) throws IOException {
        // 第一步：检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        // 第二步：把所有参数名和参数值串在一起
        StringBuilder query = new StringBuilder();
        query.append(secret);
        for (String key : keys) {
            String value = params.getString(key);
            if (null != value) {
                if (StringUtils.isNotBlank(key)) {
                    query.append(key);
                }
                if (StringUtils.isNotBlank(value)) {
                    query.append(value);
                }
            }
        }
        query.append(secret);
        // 第三步：使用MD5加密
        String sign = getMD5Str(query.toString());
        params.put(K_SIGN, sign);
    }

    /**
     * MD5 加密
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException caught!");
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException caught!");
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString();
    }
}
