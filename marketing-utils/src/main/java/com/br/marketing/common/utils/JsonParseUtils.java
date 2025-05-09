package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonParseUtils {

    /**
     * 递归查找第一个匹配的值并提前返回（支持处理字符串形式的JSON嵌套结构）
     *
     * @param obj       当前JSON对象或数组
     * @param targetKey 目标键名
     * @return 找到的第一个匹配值，未找到则返回null
     */
    public Object findFirstValueByKey(Object obj, String targetKey) {
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


    /**
     * 解析JSON结构，将数组对象解析为多个新的JSON结构
     * @param jsonStr 原始JSON字符串
     * @param arrayPath 数组在JSON中的路径，例如 "data.items"
     * @return 解析后的多个JSON对象集合
     */
    public static List<JSONObject> parseJsonArrayToMultipleObjects(String jsonStr, String arrayPath) {
        List<JSONObject> resultList = new ArrayList<>();

        try {

            // 正常处理：解析原始JSON字符串
            JSONObject originalJson = JSON.parseObject(jsonStr);

            // 获取数组路径
            // 支持简单路径，不需要使用点分隔符
            String[] pathSegments = arrayPath.contains(".") ? arrayPath.split("\\.") : new String[]{arrayPath};

            JSONObject currentObj = originalJson;

            // 遍历路径定位到数组
            for (int i = 0; i < pathSegments.length - 1; i++) {
                currentObj = currentObj.getJSONObject(pathSegments[i]);
                if (currentObj == null) {
                    return resultList; // 路径无效，返回空列表
                }
            }

            // 获取最终数组路径的最后一个部分（即数组的键名）
            String arrayKey = pathSegments[pathSegments.length - 1];

            // 获取最终数组
            JSONArray dataArray = currentObj.getJSONArray(arrayKey);
            if (dataArray == null || dataArray.isEmpty()) {
                return resultList; // 数组为空，返回空列表
            }

            // 遍历数组中的每个元素
            for (int i = 0; i < dataArray.size(); i++) {
                // 获取数组中的元素，通常是JSONObject
                Object item = dataArray.get(i);
                if (item instanceof JSONObject) {
                    // 创建新的JSON结构
                    JSONObject newJson = new JSONObject();

                    // 将原始JSON的基本信息复制到新JSON中
                    // 复制除了数组路径以外的所有属性
                    for (String key : originalJson.keySet()) {
                        if (!key.equals(pathSegments[0])) {
                            newJson.put(key, originalJson.get(key));
                        }
                    }

                    // 在新JSON中添加数组元素的内容，使用原始数组的键名
                    newJson.put(arrayKey, item);

                    // 将新的JSON对象添加到结果列表
                    resultList.add(newJson);
                }
            }
        } catch (Exception e) {
            // 日志记录异常
            log.error("解析JSON数组出错: {}", e.getMessage(), e);
        }

        return resultList;
    }

}
