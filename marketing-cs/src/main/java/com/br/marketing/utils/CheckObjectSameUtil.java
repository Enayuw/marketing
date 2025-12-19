package com.br.marketing.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CheckObjectSameUtil {

    /**
     * 字段缓存：按 Class 缓存 “字段名 -> Field”，避免频繁反射扫描
     */
    private static final ConcurrentHashMap<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 通用方法：比较两个对象的所有非静态字段值是否完全相同
     *
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @param <T>  对象类型
     * @return true=所有字段值相同，false=存在不同字段
     * @throws IllegalAccessException 反射访问字段失败时抛出
     */

    public static <T> boolean isAllFieldsEqual(T obj1, T obj2) throws IllegalAccessException {
        // 1. 处理空值情况
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }

        // 2. 检查两个对象是否为同一类型（严格类型匹配）
        Class<?> clazz1 = obj1.getClass();
        Class<?> clazz2 = obj2.getClass();
        if (!clazz1.equals(clazz2)) {
            return false;
        }

        // 3. 获取所有字段（包括父类的非静态字段）
        Field[] allFields = getAllDeclaredFields(clazz1);

        // 4. 遍历所有字段逐一比较
        for (Field field : allFields) {
            // 跳过静态字段
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // 设置字段可访问（突破private/protected限制）
            field.setAccessible(true);

            // 获取两个对象的字段值
            Object value1 = field.get(obj1);
            Object value2 = field.get(obj2);

            // 比较字段值（处理null和数组的特殊情况）
            if (!isFieldValueEqual(value1, value2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 按指定字段名比较：只要这些字段值都相同，就返回 true。
     *
     * <p>说明：</p>
     * <ul>
     *     <li>字段名支持父类字段；自动忽略 static 字段</li>
     *     <li>若 fieldNames 为空/未传，则退化为 {@link #isAllFieldsEqual(Object, Object)}</li>
     *     <li>若字段名不存在，将抛出 {@link IllegalArgumentException}（避免“字段写错”导致误判）</li>
     * </ul>
     */
    public static <T> boolean isFieldsEqual(T obj1, T obj2, String... fieldNames) {
        // 1. 空值处理
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }

        // 2. 严格类型匹配（与 isAllFieldsEqual 行为保持一致）
        Class<?> clazz1 = obj1.getClass();
        Class<?> clazz2 = obj2.getClass();
        if (!clazz1.equals(clazz2)) {
            return false;
        }

        // 3. 未指定字段：兼容老逻辑
        if (fieldNames == null || fieldNames.length == 0) {
            try {
                return isAllFieldsEqual(obj1, obj2);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("反射比较字段失败", e);
            }
        }

        Map<String, Field> fieldMap = getFieldMap(clazz1);
        for (String fieldName : fieldNames) {
            if (fieldName == null || fieldName.trim().isEmpty()) {
                continue;
            }
            Field field = fieldMap.get(fieldName);
            if (field == null) {
                throw new IllegalArgumentException("字段不存在或不可用: " + clazz1.getName() + "#" + fieldName);
            }
            try {
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);
                if (!isFieldValueEqual(value1, value2)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("反射读取字段失败: " + clazz1.getName() + "#" + fieldName, e);
            }
        }

        return true;
    }

    /**
     * 递归获取类及其所有父类的声明字段（排除Object类）
     *
     * @param clazz 目标类
     * @return 所有声明字段
     */
    private static Field[] getAllDeclaredFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Class<?> superClass = clazz.getSuperclass();

        // 递归获取父类字段（直到Object类为止）
        if (superClass != null && !superClass.equals(Object.class)) {
            Field[] superFields = getAllDeclaredFields(superClass);
            fields = concatArrays(fields, superFields);
        }

        return fields;
    }

    /**
     * 获取（并缓存）类的所有非静态字段映射：fieldName -> Field（包含父类字段）。
     */
    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, CheckObjectSameUtil::buildFieldMap);
    }

    private static Map<String, Field> buildFieldMap(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        Class<?> current = clazz;
        while (current != null && !current.equals(Object.class)) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                // 子类字段优先：如果同名覆盖，保持先放入的（子类）不被父类覆盖
                map.putIfAbsent(field.getName(), makeAccessible(field));
            }
            current = current.getSuperclass();
        }
        return Collections.unmodifiableMap(map);
    }

    private static Field makeAccessible(Field field) {
        try {
            field.setAccessible(true);
        } catch (Exception ignored) {
            // 在强封装环境下可能失败；失败时后续 get 可能抛 IllegalAccessException
        }
        return field;
    }

    /**
     * 合并两个数组
     */
    private static Field[] concatArrays(Field[] arr1, Field[] arr2) {
        Field[] result = new Field[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    /**
     * 比较单个字段的值（处理null、数组、普通对象）
     */
    private static boolean isFieldValueEqual(Object value1, Object value2) {
        // 1. 均为null
        if (value1 == null && value2 == null) {
            return true;
        }
        // 2. 其中一个为null
        if (value1 == null || value2 == null) {
            return false;
        }

        // 3. 处理数组类型
        if (value1.getClass().isArray() && value2.getClass().isArray()) {
            return isArrayEqual(value1, value2);
        }

        // 4. 普通对象（使用Objects.equals兼容基本类型包装类）
        return Objects.equals(value1, value2);
    }

    /**
     * 比较两个数组的值
     */
    private static boolean isArrayEqual(Object arr1, Object arr2) {
        // 基本类型数组
        if (arr1 instanceof boolean[]) return Arrays.equals((boolean[]) arr1, (boolean[]) arr2);
        if (arr1 instanceof byte[]) return Arrays.equals((byte[]) arr1, (byte[]) arr2);
        if (arr1 instanceof char[]) return Arrays.equals((char[]) arr1, (char[]) arr2);
        if (arr1 instanceof short[]) return Arrays.equals((short[]) arr1, (short[]) arr2);
        if (arr1 instanceof int[]) return Arrays.equals((int[]) arr1, (int[]) arr2);
        if (arr1 instanceof long[]) return Arrays.equals((long[]) arr1, (long[]) arr2);
        if (arr1 instanceof float[]) return Arrays.equals((float[]) arr1, (float[]) arr2);
        if (arr1 instanceof double[]) return Arrays.equals((double[]) arr1, (double[]) arr2);

        // 对象数组
        if (arr1 instanceof Object[]) return Arrays.equals((Object[]) arr1, (Object[]) arr2);

        // 未知数组类型
        return false;
    }
}
