package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonParseUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        //只序列化非空且非空的字符串
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 禁用转义字符
        objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    }

    /**
     * 递归查找第一个匹配的值并提前返回（支持处理字符串形式的JSON嵌套结构）
     *
     * @param obj       当前JSON对象或数组
     * @param targetKey 目标键名
     * @return 找到的第一个匹配值，未找到则返回null
     */
    public static Object findFirstValueByKey(Object obj, String targetKey) {
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
     * 递归查找匹配的值并返回（支持处理字符串形式的JSON嵌套结构）
     *
     * @param obj         当前JSON对象或数组
     * @param targetKey   目标键名
     * @param parentPath  父节点路径
     * @return 找到的第一个匹配值，未找到则返回null
     */
    public static Object findFirstValueByKey(Object obj, String targetKey, String parentPath) {
        return findFirstValueByKeyWithPath(obj, targetKey, parentPath, "");
    }

    /**
     * 递归查找匹配的值并返回（支持处理字符串形式的JSON嵌套结构）
     *
     * @param obj         当前JSON对象或数组
     * @param targetKey   目标键名
     * @param parentPath  期望的父节点路径
     * @param currentPath 当前构建的路径
     * @return 找到的第一个匹配值，未找到则返回null
     */
    private static Object findFirstValueByKeyWithPath(Object obj, String targetKey, String parentPath, String currentPath) {
        String expectedPath = processNodePaths(parentPath);
        currentPath = processNodePaths(currentPath);

        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;

            // 检查当前对象是否包含目标key
            if (jsonObj.containsKey(targetKey)) {
                // 比较当前路径是否与期望路径匹配
                if (currentPath.equals(expectedPath)) {
                    return jsonObj.get(targetKey);
                }
            }

            // 递归检查所有值
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);
                // 构建新的路径
                String newPath = StringUtils.isBlank(currentPath) ? key : currentPath + "." + key;

                // 处理嵌套的JSON字符串
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (isJsonObject(strValue)) {
                        try {
                            JSONObject nestedJson = JSONObject.parseObject(strValue);
                            Object result = findFirstValueByKeyWithPath(nestedJson, targetKey, expectedPath, newPath);
                            if (result != null) {
                                return result;
                            }
                        } catch (Exception e) {
                            // 解析失败，忽略异常，继续处理
                            log.error("JSON字符串解析失败: {}", strValue, e);
                        }
                    }
                } else if (value instanceof JSONObject) {
                    Object result = findFirstValueByKeyWithPath(value, targetKey, expectedPath, newPath);
                    if (result != null) {
                        return result;
                    }
                } else if (value instanceof JSONArray) {
                    // 对于数组，我们保持当前路径不变，因为数组元素不会改变路径
                    for (int i = 0; i < ((JSONArray) value).size(); i++) {
                        Object item = ((JSONArray) value).get(i);
                        if (item instanceof JSONObject) {
                            Object result = findFirstValueByKeyWithPath(item, targetKey, expectedPath, newPath);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            // 对于数组，我们遍历每个元素，但保持当前路径不变
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);
                if (item instanceof JSONObject) {
                    Object result = findFirstValueByKeyWithPath(item, targetKey, expectedPath, currentPath);
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
    public static boolean isJsonObject(String str) {
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
     * @param originalJson 原始JSON字符串
     * @param arrayPath 数组在JSON中的路径，例如 "data.items"
     * @return 解析后的多个JSON对象集合
     */
    public static List<JSONObject> parseJsonArrayToMultipleObjects(JSONObject originalJson, String arrayPath) {
        List<JSONObject> resultList = new ArrayList<>();

        try {
            // 获取数组路径(支持简单路径，不需要使用点分隔符)
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


    /**
     * 解析JSON结构，将数组对象解析为多个新的JSON结构
     * 该方法支持直接指定数组名称，会自动在JSON结构中查找该数组
     *
     * @param originalJson 原始JSON对象
     * @param arrayName 数组名称，例如 "items"（无需指定完整路径如"data.items"）
     * @return 解析后的多个JSON对象集合
     */
    public static List<JSONObject> parseJsonArrayByName(JSONObject originalJson, String arrayName) {
        List<JSONObject> resultList = new ArrayList<>();

        try {
            // 递归查找指定名称的数组
            Object arrayObj = findFirstValueByKey(originalJson, arrayName);
            if (!(arrayObj instanceof JSONArray)) {
                return resultList; // 未找到数组或找到的不是数组类型，返回空列表
            }

            JSONArray dataArray = (JSONArray) arrayObj;
            if (dataArray.isEmpty()) {
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
                    // 复制所有属性(排除可能包含该数组的属性)
                    for (String key : originalJson.keySet()) {
                        Object value = originalJson.get(key);
                        // 如果当前属性包含目标数组，则跳过
                        if (!(containsArray(value, arrayName))) {
                            newJson.put(key, value);
                        }
                    }

                    // 在新JSON中添加数组元素的内容，使用原始数组的键名
                    newJson.put(arrayName, item);

                    // 将新的JSON对象添加到结果列表
                    resultList.add(newJson);
                }
            }
        } catch (Exception e) {
            // 日志记录异常
            log.error("根据数组名称解析JSON出错: {}", e.getMessage(), e);
        }

        return resultList;
    }

    /**
     * 检查JSON对象或数组是否包含指定名称的数组
     *
     * @param obj JSON对象或数组
     * @param arrayName 要查找的数组名称
     * @return 是否包含指定数组
     */
    private static boolean containsArray(Object obj, String arrayName) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;

            // 检查当前对象是否包含目标数组
            if (jsonObj.containsKey(arrayName) && jsonObj.get(arrayName) instanceof JSONArray) {
                return true;
            }

            // 递归检查所有值
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    if (containsArray(value, arrayName)) {
                        return true;
                    }
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;

            // 递归检查数组中的每个元素
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);
                if (item instanceof JSONObject || item instanceof JSONArray) {
                    if (containsArray(item, arrayName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查JSON对象或数组是否包含指定名称的数组
     *
     * @param arrayName 要查找的数组名称
     * @return 是否包含指定数组
     */
    public static String processNodePaths(String arrayName) {
        // 处理特殊的父节点路径
        String expectedPath = StringUtils.isNotBlank(arrayName) ? arrayName : "";
        if (expectedPath.contains("dataItems.item.")) {
            expectedPath = expectedPath.replace("dataItems.item.", "");
        }
        if (expectedPath.contains("dataItems")) {
            expectedPath = expectedPath.replace("dataItems", "");
        }
        if (expectedPath.contains("item.")) {
            expectedPath = expectedPath.replace("item.", "");
        }
        if (expectedPath.contains("item")) {
            expectedPath = expectedPath.replace("item", "");
        }
        // 去除开头和结尾的点
        expectedPath = expectedPath.replaceAll("^\\.|\\.$", "");

        return expectedPath;
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }



}
