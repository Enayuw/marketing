package com.br.marketing.common.utils.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public final class CallUtils {
    /**
     * 获取javaBean的form-urlencoded拼接字符串
     */
    public static String getFormUrlEncodedStr(Object javaBean, String encodeName) {
        if (javaBean == null) {
            return "";
        }

        if (javaBean instanceof String) {
            return (String) javaBean;
        }

        Map<String, Object> paramKV = getPropertiesMap(javaBean);
        return getFormUrlEncodedStr(paramKV, false, encodeName);
    }

    public static String getFormUrlEncodedStr(Object javaBean, String encodeName,Boolean isEncode) {
        if (javaBean == null) {
            return "";
        }

        if (javaBean instanceof String) {
            return (String) javaBean;
        }

        Map<String, Object> paramKV = getPropertiesMap(javaBean);
        return getFormUrlEncodedStr(paramKV,isEncode, encodeName);
    }

    /**
     * 获取javaBean的form-urlencoded拼接字符串
     */
    public static String getFormUrlEncodedStr(Object javaBean) {
        if (javaBean == null) {
            return "";
        }

        if (javaBean instanceof String) {
            return (String) javaBean;
        }

        Map<String, Object> paramKV = getPropertiesMap(javaBean);
        return getFormUrlEncodedStr(paramKV, false, null);
    }

    /**
     * 获取javaBean的form-urlencoded拼接字符串
     */
    public static String getFormUrlEncodedStr(Map<String, Object> paramKV, boolean toUrlEncoded, String encodeName) {
        ArrayList<String> paramList = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : paramKV.entrySet()) {
            try {
                String valueStr = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
                paramList.add(String.format("%s=%s", entry.getKey(),
                        toUrlEncoded ? urlEncode(valueStr, encodeName) : valueStr));


            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        String paramJointStr = StringUtils.join(paramList, "&");

        return paramJointStr;
    }

    public static String encodeRawQuery(String rawQueryStr, String encodeName) {
        if (rawQueryStr == null) {
            return null;
        }

        String encodedQueryStr = "";
        StringBuilder encodedQuery = new StringBuilder();
        if (StringUtils.isNotEmpty(rawQueryStr)) {
            String[] keyValues = StringUtils.split(rawQueryStr, '&');
            if (keyValues != null) {
                for (String keyVal : keyValues) {
                    String[] oneKeyVal = StringUtils.split(keyVal, '=');
                    if (oneKeyVal != null && oneKeyVal.length > 1) {
                        String encodeStr = null;
                        try {
                            // 先解码再编码，防止有些参数已经编码有些未编码的情况
                            encodeStr = URLEncoder.encode(URLDecoder.decode(oneKeyVal[1], encodeName), encodeName);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        encodedQuery.append(String.format("&%s=%s", oneKeyVal[0], encodeStr));
                    }
                }
                encodedQueryStr = encodedQuery.toString().substring(1);
            }
        }
        return encodedQueryStr;
    }

    private static String urlEncode(String valueStr, String encodeName) throws UnsupportedEncodingException {
        String encodedStr = URLEncoder.encode(valueStr, encodeName);
        // 将本不应替换的“%”替换回来（%25为%的encoded值）
//        return encodedStr.replace("%25", "%");
        return encodedStr;
    }

    /**
     * 获取javaBean的属性map
     */
    public static Map<String, Object> getPropertiesMap(Object javaBean) {
        if (javaBean instanceof Map) {
            return (Map<String, Object>) javaBean;
        }

        return (JSONObject) JSON.toJSON(javaBean);
    }

    /**
     * 反序列化Json
     */
    public static <T> T deserializeJson(Class<T> clazz, String json) {
        return JSON.parseObject(json, clazz);
    }

    /**
     * 序列化Json
     */
    public static <T> String serializeJson(T bean) {
        return JSON.toJSONString(bean);
    }

    /**
     * 获取javaBean的form-data拼接字符串
     */
    public static MultiValueMap<String, Object> getFormDataMap(Object javaBean) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        if (javaBean == null) {
            return form;
        }

        if (javaBean instanceof Map) {
            form.setAll((Map<String, Object>) javaBean);
        } else {
            form.setAll(BeanUtils.transBean2Map(javaBean));
        }

        return form;
    }
}

