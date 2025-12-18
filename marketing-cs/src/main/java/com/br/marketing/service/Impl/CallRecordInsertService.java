package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.CallRecordLLMResultV2Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 通话记录插入服务（独立事务）
 */
@Service
@Slf4j
public class CallRecordInsertService {

    @Autowired
    private CallRecordLLMResultV2Mapper callRecordLLMResultV2Mapper;

    /**
     * 带事务的插入操作，方法返回后事务已提交
     */
    @Transactional(rollbackFor = Exception.class)
    public Long insertData(String tableName, JSONObject jsonObject) {
        String insertSql = buildInsertSqlByJson(tableName, jsonObject);
        Map<String, Object> resultMap = new HashMap<>();
        callRecordLLMResultV2Mapper.insertData(insertSql, resultMap);
        Long versionRecordId = resultMap.get("id") != null ? ((Number) resultMap.get("id")).longValue() : null;
        log.warn("[通用大模型回调]插入版本明细表成功，tableName={}, versionRecordId={}", tableName, versionRecordId);
        return versionRecordId;
    }

    /**
     * 根据JSON动态构建插入SQL
     */
    private String buildInsertSqlByJson(String tableName, JSONObject jsonObject) {
        StringBuilder sql = new StringBuilder();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        // 用于记录已添加的列名，避免重复
        Set<String> addedColumns = new HashSet<>();

        // 遍历JSON中的所有字段，生成INSERT语句
        for (String key : jsonObject.keySet()) {

            Object value = jsonObject.get(key);

            // 如果detail字段是JSONObject，需要展开其内部字段
            if ("detail".equals(key) && value instanceof JSONObject) {
                JSONObject detailObj = (JSONObject) value;

                // 先添加detail字段本身（json类型）
                String columnName = camelToSnake(key);
                if (!addedColumns.contains(columnName)) {
                    columns.append("`").append(columnName).append("`,");
                    // JSON对象转为JSON字符串
                    String jsonStr = JSON.toJSONString(value);
                    jsonStr = jsonStr.replace("\\", "\\\\").replace("'", "\\'");
                    values.append("'").append(jsonStr).append("',");
                    addedColumns.add(columnName);
                }

                // 遍历detail里的所有字段，作为独立列插入
                for (String detailKey : detailObj.keySet()) {
                    Object detailValue = detailObj.get(detailKey);
                    String detailColumnName = camelToSnake(detailKey);

                    // 避免与外层字段冲突，如果冲突则跳过（外层字段优先）
                    if (!addedColumns.contains(detailColumnName) && detailValue != null) {
                        columns.append("`").append(detailColumnName).append("`,");
                        appendValue(values, detailValue);
                        addedColumns.add(detailColumnName);
                    }
                }
            } else if (value != null) {
                // 普通字段处理
                String columnName = camelToSnake(key);
                if (!addedColumns.contains(columnName)) {
                    columns.append("`").append(columnName).append("`,");
                    appendValue(values, value);
                    addedColumns.add(columnName);
                }
            }
        }

        // 默认增加 receive_date 字段，值为 LocalDate.now()
        String receiveDateColumn = "receive_date";
        if (!addedColumns.contains(receiveDateColumn)) {
            columns.append("`").append(receiveDateColumn).append("`,");
            String dateValue = LocalDate.now().toString();
            values.append("'").append(dateValue).append("',");
            addedColumns.add(receiveDateColumn);
        }

        // 移除最后的逗号
        if (columns.length() > 0 && columns.charAt(columns.length() - 1) == ',') {
            columns.setLength(columns.length() - 1);
        }
        if (values.length() > 0 && values.charAt(values.length() - 1) == ',') {
            values.setLength(values.length() - 1);
        }

        sql.append("INSERT INTO `")
                .append(tableName)
                .append("` (")
                .append(columns)
                .append(") VALUES (")
                .append(values)
                .append(")");

        return sql.toString();
    }

    /**
     * 追加值到values字符串
     */
    private void appendValue(StringBuilder values, Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            // 转义单引号和反斜杠，防止SQL注入
            strValue = strValue.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(strValue).append("',");
        } else if (value instanceof Number || value instanceof Boolean) {
            values.append(value).append(",");
        } else if (value instanceof JSONObject || value instanceof Map) {
            // JSON对象转为JSON字符串
            String jsonStr = JSON.toJSONString(value);
            jsonStr = jsonStr.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(jsonStr).append("',");
        } else {
            // 其他类型转为字符串
            String strValue = String.valueOf(value);
            strValue = strValue.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(strValue).append("',");
        }
    }

    /**
     * 驼峰命名转下划线命名
     */
    private String camelToSnake(String camelCase) {
        if (StringUtils.isEmpty(camelCase)) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}

