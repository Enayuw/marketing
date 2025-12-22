package com.br.marketing.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class CheckObjectSameUtil {

    /**
     * 字段缓存：按 Class 缓存 “字段名 -> Field”，避免频繁反射扫描
     */
    private static final ConcurrentHashMap<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 字段缓存：按 Class 缓存 “所有实例字段 Field[]（包含父类字段，且保留同名隐藏字段）”。
     * <p>注意：与 {@link #FIELD_CACHE} 不同，这里不能用 fieldName 去重，否则会改变同名隐藏字段的比较行为。</p>
     */
    private static final ConcurrentHashMap<Class<?>, Field[]> ALL_INSTANCE_FIELDS_CACHE = new ConcurrentHashMap<>();

    private CheckObjectSameUtil() {
    }

    /**
     * 通用方法：比较两个对象的所有非静态字段值是否完全相同（包含父类字段）。
     *
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @param <T>  对象类型
     * @return true=所有字段值相同，false=存在不同字段
     * @throws IllegalAccessException 反射访问字段失败时抛出
     */

    public static <T> boolean isAllFieldsEqual(T obj1, T obj2) throws IllegalAccessException {
        Class<?> clazz = strictSameClassOrNull(obj1, obj2);
        if (clazz == null) {
            return obj1 == null && obj2 == null;
        }

        Field[] allFields = getAllInstanceFields(clazz);
        for (Field field : allFields) {
            Object value1 = field.get(obj1);
            Object value2 = field.get(obj2);
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
        Class<?> clazz1 = strictSameClassOrNull(obj1, obj2);
        if (clazz1 == null) {
            return obj1 == null && obj2 == null;
        }

        // 未指定字段：兼容老逻辑
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
     * 严格类型匹配：仅当 obj1/obj2 都非空且 getClass() 相等时返回该 Class，否则返回 null。
     */
    private static Class<?> strictSameClassOrNull(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return null;
        }
        Class<?> clazz1 = obj1.getClass();
        if (!clazz1.equals(obj2.getClass())) {
            return null;
        }
        return clazz1;
    }

    /**
     * 获取（并缓存）类的所有非静态字段映射：fieldName -> Field（包含父类字段）。
     */
    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, CheckObjectSameUtil::buildFieldMap);
    }

    /**
     * 获取（并缓存）类的所有实例字段数组（包含父类字段，且保留同名隐藏字段）。
     * <p>顺序：子类在前、父类在后。</p>
     */
    private static Field[] getAllInstanceFields(Class<?> clazz) {
        return ALL_INSTANCE_FIELDS_CACHE.computeIfAbsent(clazz, CheckObjectSameUtil::buildAllInstanceFields);
    }

    private static Field[] buildAllInstanceFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && !current.equals(Object.class)) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                result.add(makeAccessible(field));
            }
            current = current.getSuperclass();
        }
        return result.toArray(new Field[0]);
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
        // 数组类型不同，直接不相等
        if (!arr1.getClass().equals(arr2.getClass())) {
            return false;
        }
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
