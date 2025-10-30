package com.br.marketing.bridge.job.dingding;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bridge.client.DingDingAiTableClient;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 钉钉AI表格数据同步作业
 * @Author hong.chen
 * @CreateTime 2025/10/29
 */
@Component
@Slf4j
public class DingDingTableSyncToDbJob extends AbstractSimpleElasticJob {
    
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    
    @Resource
    private DingDingAiTableClient dingDingAiTableClient;
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        log.warn("钉钉AI表格数据同步作业开始执行");
        long startTime = System.currentTimeMillis();
        
        try {
            JSONObject dingDingTableConfig = marketingCommonConfig.getDingDingTableConfig();
            if (dingDingTableConfig == null || dingDingTableConfig.isEmpty()) {
                log.warn("钉钉表格配置为空，跳过执行");
                return;
            }
            
            // 遍历配置的所有表
            for (Map.Entry<String, Object> entry : dingDingTableConfig.entrySet()) {
                String tableName = entry.getKey();
                JSONObject tableConfig = (JSONObject) entry.getValue();
                
                try {
                    syncTableData(tableName, tableConfig);
                } catch (Exception e) {
                    log.error("同步表{}数据异常", tableName, e);
//                    saveExceptionRecord(tableName, null, e.getMessage());
                }
            }
            
            long endTime = System.currentTimeMillis();
            log.warn("钉钉AI表格数据同步作业执行完成，耗时:{}ms", (endTime - startTime));
        } catch (Exception e) {
            log.error("钉钉AI表格数据同步作业执行异常", e);
        }
    }
    
    /**
     * 同步单个表的数据
     * 
     * @param tableName 表名
     * @param tableConfig 表配置
     */
    private void syncTableData(String tableName, JSONObject tableConfig) {
        log.warn("开始同步表数据，表名: {}", tableName);
        
        // 获取配置参数
        String appKey = tableConfig.getString("appKey");
        String appSecret = tableConfig.getString("appSecret");
        String operatorId = tableConfig.getString("operatorId");
        String baseId = tableConfig.getString("baseId");
        String sheetId = tableConfig.getString("sheetId");
        
        // 获取字段顺序配置（可选）
        JSONArray fieldOrder = tableConfig.getJSONArray("fieldOrder");
        
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(appSecret) || 
            StringUtils.isEmpty(baseId) || StringUtils.isEmpty(sheetId)) {
            log.warn("表{}配置参数不完整，跳过同步", tableName);
            return;
        }
        
        // 获取AccessToken
        String accessToken = dingDingAiTableClient.getAccessToken(appKey, appSecret);
        if (StringUtils.isEmpty(accessToken)) {
            log.error("获取AccessToken失败，跳过同步表: {}", tableName);
            return;
        }
        
        // 获取表格字段信息
        List<Map<String, Object>> fields = dingDingAiTableClient.getSheetFields(
                accessToken, baseId, sheetId, operatorId);
        
        if (CollectionUtils.isEmpty(fields)) {
            log.warn("获取表{}的字段信息失败，跳过同步", tableName);
            return;
        }
        
        // 构建字段名称映射（钉钉字段名 -> 数据库字段名）
        List<String> fieldNames = new ArrayList<>();
        if (fieldOrder != null && !fieldOrder.isEmpty()) {
            // 使用配置的字段顺序
            fieldNames = fieldOrder.toJavaList(String.class);
        } else {
            // 使用钉钉返回的字段顺序
            fieldNames = fields.stream()
                    .map(f -> (String) f.get("name"))
                    .collect(Collectors.toList());
        }
        
        log.warn("表{}字段顺序: {}", tableName, fieldNames);
        
        // 获取所有数据记录
        List<List<Object>> allRecords = fetchAllRecords(accessToken, baseId, sheetId, operatorId, fieldNames);
        
        if (CollectionUtils.isEmpty(allRecords)) {
            log.warn("表{}没有数据记录，跳过写入", tableName);
            return;
        }
        
        log.warn("表{}获取到{}条数据记录", tableName, allRecords.size());
        
        // 删除旧数据
        String deleteSql = String.format("DELETE FROM %s", tableName);
        int deleteCount = jdbcTemplate.update(deleteSql);
        log.warn("删除表{}旧数据，删除条数: {}", tableName, deleteCount);
        
        // 批量插入新数据
        batchInsertRecords(tableName, fieldNames, allRecords);
        
        log.warn("表{}数据同步完成，插入条数: {}", tableName, allRecords.size());
    }
    
    /**
     * 获取所有数据记录（分页查询）
     * 
     * @param accessToken 访问令牌
     * @param baseId Base ID
     * @param sheetId Sheet ID
     * @param operatorId 操作人ID
     * @param fieldNames 字段名称列表
     * @return 所有记录
     */
    private List<List<Object>> fetchAllRecords(String accessToken, String baseId, String sheetId, 
                                                String operatorId, List<String> fieldNames) {
        List<List<Object>> allRecords = new ArrayList<>();
        String nextToken = null;
        int pageNum = 0;
        
        do {
            pageNum++;
            JSONObject response = dingDingAiTableClient.getSheetRecords(
                    accessToken, baseId, sheetId, operatorId, nextToken, 100);
            
            if (response == null) {
                log.error("获取数据记录失败，第{}页", pageNum);
                break;
            }
            
            JSONArray records = response.getJSONArray("records");
            if (records == null || records.isEmpty()) {
                break;
            }
            
            log.warn("获取第{}页数据，记录数: {}", pageNum, records.size());
            
            // 解析每条记录
            for (int i = 0; i < records.size(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fieldsData = record.getJSONObject("fields");
                
                // 按照字段顺序提取值
                List<Object> rowValues = extractRowValues(fieldsData, fieldNames);
                
                // 过滤空行
                if (!isEmptyRow(rowValues)) {
                    allRecords.add(rowValues);
                }
            }
            
            // 检查是否有下一页
            Boolean hasMore = response.getBoolean("hasMore");
            nextToken = hasMore != null && hasMore ? response.getString("nextToken") : null;
            
        } while (!StringUtils.isEmpty(nextToken));
        
        return allRecords;
    }
    
    /**
     * 按照字段顺序提取一行数据的值
     * 
     * @param fieldsData 字段数据
     * @param fieldNames 字段名称列表
     * @return 值列表
     */
    private List<Object> extractRowValues(JSONObject fieldsData, List<String> fieldNames) {
        List<Object> values = new ArrayList<>();
        
        for (String fieldName : fieldNames) {
            Object value = fieldsData.get(fieldName);
            
            // 处理日期类型
            if (value instanceof Number) {
                // 可能是时间戳，尝试格式化
                try {
                    long timestamp = ((Number) value).longValue();
                    // 判断是否是时间戳（毫秒）
                    if (timestamp > 10000000000L) {
                        Instant instant = Instant.ofEpochMilli(timestamp);
                        value = formatByDingDingFormatter(instant, null);
                    }
                } catch (Exception e) {
                    // 保持原值
                }
            }
            
            // 转换为字符串（除了null）
            values.add(value == null ? null : String.valueOf(value));
        }
        
        return values;
    }
    
    /**
     * 批量插入记录
     * 
     * @param tableName 表名
     * @param fieldNames 字段名称列表
     * @param records 记录列表
     */
    private void batchInsertRecords(String tableName, List<String> fieldNames, List<List<Object>> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        
        // 构建INSERT SQL
        String columns = String.join(", ", fieldNames);
        String placeholders = fieldNames.stream().map(f -> "?").collect(Collectors.joining(", "));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
        
        log.warn("批量插入SQL: {}", sql);
        
        // 分批插入（每批500条）
        int batchSize = 500;
        int totalBatches = (records.size() + batchSize - 1) / batchSize;
        
        for (int i = 0; i < totalBatches; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min((i + 1) * batchSize, records.size());
            List<List<Object>> batch = records.subList(fromIndex, toIndex);
            
            try {
                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(java.sql.PreparedStatement ps, int j) throws java.sql.SQLException {
                        List<Object> row = batch.get(j);
                        for (int k = 0; k < row.size(); k++) {
                            ps.setObject(k + 1, row.get(k));
                        }
                    }
                    
                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
                log.warn("批量插入第{}/{}批，插入条数: {}", i + 1, totalBatches, batch.size());
            } catch (Exception e) {
                log.error("批量插入第{}批失败", i + 1, e);
                saveExceptionRecord(tableName, JSON.toJSONString(batch), e.getMessage());
            }
        }
    }
    
    /**
     * 判断是否为空行（所有字段都为空）
     * 
     * @param rowValues 行值列表
     * @return true-空行，false-非空行
     */
    private boolean isEmptyRow(List<Object> rowValues) {
        if (CollectionUtils.isEmpty(rowValues)) {
            return true;
        }
        
        for (Object value : rowValues) {
            if (value != null && !StringUtils.isEmpty(String.valueOf(value).trim())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 格式化钉钉日期数据
     */
    private String formatByDingDingFormatter(Instant instant, String formatter) {
        if (formatter == null) {
            // 默认格式化为日期时间
            LocalDateTime dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        LocalDateTime dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate date = dateTime.toLocalDate();
        
        switch (formatter) {
            case "YYYY-MM-DD":
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
            case "YYYY-MM-DD HH:mm":
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                
            case "YYYY-MM-DD HH:mm:ss":
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
            case "YYYY/MM/DD":
                return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                
            case "YYYY/MM/DD HH:mm":
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                
            case "YYYY/MM/DD HH:mm:ss":
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
                
            case "YYYY年MM月DD日":
                return date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
                
            case "YYYY年MM月":
                return date.format(DateTimeFormatter.ofPattern("yyyy年MM月"));
                
            case "MM月DD日":
                return date.format(DateTimeFormatter.ofPattern("MM月dd日"));
                
            default:
                log.warn("未知的formatter格式: {}, 使用默认格式", formatter);
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    /**
     * 保存异常记录
     * 
     * @param tableName 表名
     * @param jsonData 数据JSON
     * @param reason 异常原因
     */
    private void saveExceptionRecord(String tableName, String jsonData, String reason) {
        try {
            // 根据表名判断类型
            Integer type = tableName.contains("sms") ? 1 : 2;
            
            String sql = "INSERT INTO b_cost_price_ex_record (type, json_data, reason, extend) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, type, jsonData, reason, tableName);
        } catch (Exception e) {
            log.error("保存异常记录失败", e);
        }
    }
}
