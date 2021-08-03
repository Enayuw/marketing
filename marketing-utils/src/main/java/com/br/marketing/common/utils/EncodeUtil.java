package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * JSON字符串加密解密工具
 */
@Slf4j
public class EncodeUtil {
    private static final String DEFAULTKEYS = "idCard,cell";
    private static Set<String> set = new HashSet();
    private static final int ENCODE_TYPE = 1;
    private static final int DECODE_TYPE = 0;

    /**
     * json字符串解密
     *
     * @param jsonStr 待解密的json字符串
     * @param keys    待解密的key
     * @return
     */
    public static String decode(String jsonStr, String keys) {
        if (StringUtils.isEmpty(keys)) {
            return jsonStr;
        }
        return core(keys, jsonStr, DECODE_TYPE);
    }

    /**
     * json字符串加密
     *
     * @param jsonStr 待加密的json字符串
     * @param keys    待加密的key
     * @return
     */
    public static String encode(String jsonStr, String keys) {
        if (StringUtils.isEmpty(keys)) {
            return jsonStr;
        }
        return core(keys, jsonStr, ENCODE_TYPE);
    }

    /**
     * 默认加密json字符串  默认针对 idCard、cell进行加密
     *
     * @param jsonStr
     * @return
     */
    public static String encodeDefault(String jsonStr) {
        return encode(jsonStr, DEFAULTKEYS);
    }

    /**
     * 默认解密json字符串  默认针对 idCard、cell进行解密
     *
     * @param jsonStr
     * @return
     */
    public static String decodeDefault(String jsonStr) {
        return decode(jsonStr, DEFAULTKEYS);
    }

    /**
     * 核心加密解密方法
     *
     * @param keys    加密或者解密的key
     * @param jsonStr json字符串
     * @param method  加密或者解密类型 1 加密 0 解密
     * @return
     */
    private static String core(String keys, String jsonStr, int method) {
        if (StringUtils.isEmpty(keys)) {
            return jsonStr;
        }
        set = getEncodeKeySet(keys);
        try {
            if (JSON.parse(jsonStr) instanceof JSONObject) {
                JSONObject jsonObj = JSON.parseObject(jsonStr);
                analysisJson(jsonObj, method, keys);
                set.clear();
                String str = jsonObj.toString();
                return str;
            } else if (JSON.parse(jsonStr) instanceof JSONArray) {
                JSONArray jsonArray = JSON.parseArray(jsonStr);
                for (int i = 0; i < jsonArray.size(); i++) {
                    analysisJson(jsonArray.get(i), method, keys);
                }
                set.clear();
                String str = jsonArray.toString();
                return str;
            }
        } catch (Exception e) {
            log.error("core error", e);
        }
        return jsonStr;
    }

    /**
     * 递归分析json字符串并进行处理
     *
     * @param obj
     * @param metodType
     * @param keys
     */
    private static void analysisJson(Object obj, int metodType, String keys) {
        //如果obj为json数组
        if (obj instanceof JSONArray) {
            JSONArray objArray = (JSONArray) obj;
            for (int i = 0; i < objArray.size(); i++) {
                analysisJson(objArray.get(i), metodType, keys);
            }
        }
        //如果为json对象
        else if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            Iterator it = jsonObject.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                Object object = jsonObject.get(key);
                //如果得到的是数组
                if (object instanceof JSONArray) {
                    JSONArray objArray = (JSONArray) object;
                    analysisJson(objArray, metodType, keys);
                }
                //如果key中是一个json对象
                else if (object instanceof JSONObject) {
                    analysisJson((JSONObject) object, metodType, keys);
                } else {
                    if (set.contains(key)) {
                        if (metodType == ENCODE_TYPE) {
                            jsonObject.put(key, BrCipherMaker.getInstance().encode(object.toString()));
                        } else if (metodType == DECODE_TYPE) {
                            jsonObject.put(key, BrCipherMaker.getInstance().decode(object.toString()));
                        }
                    }
                }
            }
        }
    }

    private static Set<String> getEncodeKeySet(String keys) {
        String[] arr = keys.split(",");
        for (String str : arr) {
            if (str != null && str.trim().length() > 0) {
                set.add(str);
            }
        }
        return set;
    }
}