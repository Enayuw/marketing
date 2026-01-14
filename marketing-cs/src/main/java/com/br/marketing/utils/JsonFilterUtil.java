package com.br.marketing.utils;

import com.br.marketing.vo.autocheck.AutoCheckResultVO;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonFilterUtil {

    private JsonFilterUtil() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String JSON_DYNAMIC_FILTER_ID = "autoCheckDynamicFieldFilter";

    @JsonFilter(JSON_DYNAMIC_FILTER_ID)
    private interface DynamicFieldFilterMixIn {
    }

    /**
     * 对象转 JSON 字符串，并按需排除字段（仅对“对象属性”生效；如果传入的是 List/Map 需要按元素/值类型做过滤）。
     *
     * <p>示例：{@code JsonFilterUtil.toJsonSafe(latest, "id", "createTime")}</p>
     */
    public static String toJsonExcludeSafe(Object obj, String... excludeFields) {
        if (obj == null) {
            return "";
        }
        try {
            if (excludeFields == null || excludeFields.length == 0) {
                return OBJECT_MAPPER.writeValueAsString(obj);
            }

            // 注意：不要在共享的 OBJECT_MAPPER 上做 addMixIn / setFilterProvider（配置变更非线程安全），这里用 copy()
            ObjectMapper mapper = OBJECT_MAPPER.copy();
            mapper.addMixIn(obj.getClass(), DynamicFieldFilterMixIn.class);

            Set<String> excludeSet = Arrays.stream(excludeFields)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter(JSON_DYNAMIC_FILTER_ID,
                            SimpleBeanPropertyFilter.serializeAllExcept(excludeSet));

            return mapper.writer(filters).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    /**
     * 对象转 JSON 字符串，并仅序列化指定字段（仅对“对象属性”生效；不支持 a.b 这种嵌套路径）。
     *
     * <p>示例：{@code JsonFilterUtil.toJsonIncludeSafe(latest, "apiCode", "snapTime")}</p>
     */
    public static String toJsonIncludeSafe(Object obj, String... includeFields) {
        if (obj == null) {
            return "";
        }
        try {
            if (includeFields == null || includeFields.length == 0) {
                return OBJECT_MAPPER.writeValueAsString(obj);
            }

            Set<String> includeSet = Arrays.stream(includeFields)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            if (includeSet.isEmpty()) {
                return OBJECT_MAPPER.writeValueAsString(obj);
            }

            ObjectMapper mapper = OBJECT_MAPPER.copy();
            mapper.addMixIn(obj.getClass(), DynamicFieldFilterMixIn.class);

            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter(JSON_DYNAMIC_FILTER_ID,
                            SimpleBeanPropertyFilter.filterOutAllExcept(includeSet));

            return mapper.writer(filters).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }
}


