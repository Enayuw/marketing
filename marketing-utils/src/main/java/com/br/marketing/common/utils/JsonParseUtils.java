package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonParseUtils {

    /**
     * 递归查找第一个匹配的值并提前返回（支持处理字符串形式的JSON嵌套结构）
     *
     * @param obj       当前JSON对象或数组
     * @param targetKey 目标键名
     * @return 找到的第一个匹配值，未找到则返回null
     */
    private Object findFirstValueByKey(Object obj, String targetKey) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;

            // 检查当前对象是否包含目标key
            if (jsonObj.containsKey(targetKey)) {
                return jsonObj.get(targetKey);
            }

            // 递归检查所有值
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);

                // 处理嵌套的JSON字符串
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (isJsonObject(strValue)) {
                        try {
                            JSONObject nestedJson = JSONObject.parseObject(strValue);
                            Object result = findFirstValueByKey(nestedJson, targetKey);
                            if (result != null) {
                                return result;
                            }
                        } catch (Exception e) {
                            // 解析失败，忽略异常，继续处理
                            log.error("JSON字符串解析失败: {}", strValue, e);
                            return null;
                        }
                    }
                } else if (value instanceof JSONObject || value instanceof JSONArray) {
                    Object result = findFirstValueByKey(value, targetKey);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;

            // 递归检查数组中的每个元素
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);

                // 处理嵌套的JSON字符串
                if (item instanceof String) {
                    String strValue = (String) item;
                    if (isJsonObject(strValue)) {
                        try {
                            JSONObject nestedJson = JSONObject.parseObject(strValue);
                            Object result = findFirstValueByKey(nestedJson, targetKey);
                            if (result != null) {
                                return result;
                            }
                        } catch (Exception e) {
                            // 解析失败，忽略异常，继续处理
                            log.error("JSON字符串解析失败: {}", strValue, e);
                            return null;
                        }
                    }
                } else if (item instanceof JSONObject || item instanceof JSONArray) {
                    Object result = findFirstValueByKey(item, targetKey);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 判断字符串是否为JSON对象
     *
     * @param str 待检查的字符串
     * @return 是否为JSON对象
     */
    private boolean isJsonObject(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
