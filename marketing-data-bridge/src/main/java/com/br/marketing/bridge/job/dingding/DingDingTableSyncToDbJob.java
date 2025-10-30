package com.br.marketing.bridge.job.dingding;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bridge.client.DingDingAiTableClient;
import com.br.marketing.mapper.DingDingTableSyncMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
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
    private DingDingTableSyncMapper dingDingTableSyncMapper;
    
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
        
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(appSecret) || 
            StringUtils.isEmpty(baseId) || StringUtils.isEmpty(sheetId)) {
            log.warn("表{}配置参数不完整，跳过同步", tableName);
            return;
        }
        
        // 1. 查询数据库表建表语句
        Map<String, Object> createTableResult = dingDingTableSyncMapper.getCreateTableSql(tableName);
        if (CollectionUtils.isEmpty(createTableResult)) {
            log.warn("获取表{}建表语句失败，跳过同步", tableName);
            return;
        }
        
        // SHOW CREATE TABLE 返回的Map中，key为"Create Table"，value为建表SQL
        String createTableSql = (String) createTableResult.get("Create Table");
        if (StringUtils.isEmpty(createTableSql)) {
            log.warn("解析表{}建表语句失败，跳过同步", tableName);
            return;
        }
        
        log.warn("表{}建表语句: {}", tableName, createTableSql.substring(0, Math.min(200, createTableSql.length())) + "...");
        
        // 2. 解析建表语句，获取字段和注释
        // 并区分业务字段和系统字段
        Map<String, String> commentToFieldMap = new LinkedHashMap<>();  // 中文 -> 英文
        List<String> businessFields = new ArrayList<>();  // 业务字段（id后到created_by前）
        List<String> systemFields = new ArrayList<>();    // 系统字段（created_by及之后）
        
        parseCreateTableSql(createTableSql, commentToFieldMap, businessFields, systemFields);
        
        log.warn("表{}业务字段: {}", tableName, businessFields);
        log.warn("表{}系统字段: {}", tableName, systemFields);
        log.warn("表{}中文列名映射: {}", tableName, commentToFieldMap);
        
        // 3. 获取AccessToken
        String accessToken = dingDingAiTableClient.getAccessToken(appKey, appSecret);
        if (StringUtils.isEmpty(accessToken)) {
            log.error("获取AccessToken失败，跳过同步表: {}", tableName);
            return;
        }
        
        // 4. 获取钉钉数据记录
        List<String> allFieldNames = new ArrayList<>(businessFields);
        allFieldNames.addAll(systemFields);
        List<Map<String, Object>> allRecords = fetchAllRecordsWithMapping(
                accessToken, baseId, sheetId, operatorId, commentToFieldMap, systemFields);
        
        if (CollectionUtils.isEmpty(allRecords)) {
            log.warn("表{}没有数据记录，跳过写入", tableName);
            return;
        }
        
        log.warn("表{}获取到{}条数据记录", tableName, allRecords.size());
        
        // 5. 删除旧数据
        int deleteCount = dingDingTableSyncMapper.deleteAll(tableName);
        log.warn("删除表{}旧数据，删除条数: {}", tableName, deleteCount);
        
        // 6. 批量插入新数据
        batchInsertRecords(tableName, allFieldNames, allRecords);
        
        log.warn("表{}数据同步完成，插入条数: {}", tableName, allRecords.size());
    }
    
    /**
     * 解析建表语句，提取字段和注释
     * 
     * @param createTableSql 建表语句
     * @param commentToFieldMap 中文注释到英文字段名的映射（输出）
     * @param businessFields 业务字段列表（输出）
     * @param systemFields 系统字段列表（输出）
     */
    private void parseCreateTableSql(String createTableSql, Map<String, String> commentToFieldMap,
                                      List<String> businessFields, List<String> systemFields) {
        // 正则表达式匹配字段定义：`字段名` 类型 [约束] [comment '注释']
        // 示例：`line_supplier` varchar(255) null comment '供应商名称',
        String fieldPattern = "`(\\w+)`[^,]*?(?:comment\\s+'([^']*)')?[,)]";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(fieldPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(createTableSql);
        
        boolean isBusinessField = false;
        boolean isSystemField = false;
        
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldComment = matcher.group(2);
            
            // 跳过id字段
            if ("id".equalsIgnoreCase(fieldName)) {
                isBusinessField = true;
                continue;
            }
            
            // created_by及之后的字段是系统字段
            if ("created_by".equalsIgnoreCase(fieldName)) {
                isSystemField = true;
                isBusinessField = false;
            }
            
            if (isBusinessField && !isSystemField) {
                businessFields.add(fieldName);
                if (!StringUtils.isEmpty(fieldComment)) {
                    commentToFieldMap.put(fieldComment, fieldName);
                }
            } else if (isSystemField) {
                systemFields.add(fieldName);
            }
        }
    }
    
    /**
     * 获取所有数据记录（分页查询，带字段映射）
     * 
     * @param accessToken 访问令牌
     * @param baseId Base ID
     * @param sheetId Sheet ID
     * @param operatorId 操作人ID
     * @param commentToFieldMap 中文列名到英文字段名的映射
     * @param systemFields 系统字段列表
     * @return 所有记录（Map形式）
     */
    private List<Map<String, Object>> fetchAllRecordsWithMapping(String accessToken, String baseId, String sheetId, 
                                                                  String operatorId, Map<String, String> commentToFieldMap,
                                                                  List<String> systemFields) {
        List<Map<String, Object>> allRecords = new ArrayList<>();
        String nextToken = null;
        int pageNum = 0;
        
        // 用户信息缓存，避免重复调用钉钉接口
        Map<String, String> unionIdToUserIdCache = new HashMap<>();  // unionId -> userId
        Map<String, String> userIdToNameCache = new HashMap<>();     // userId -> userName
        
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
                
                // 映射数据：中文列名 -> 英文字段名
                Map<String, Object> rowData = new LinkedHashMap<>();
                
                // 1. 处理业务字段（从fields中用中文列名取值）
                for (Map.Entry<String, String> entry : commentToFieldMap.entrySet()) {
                    String chineseColumnName = entry.getKey();  // 中文列名（COMMENT）
                    String englishFieldName = entry.getValue(); // 英文字段名
                    
                    Object value = fieldsData.get(chineseColumnName);
                    
                    // 处理选择类型（singleSelect等），取name值
                    if (value instanceof JSONObject) {
                        JSONObject valueObj = (JSONObject) value;
                        String name = valueObj.getString("name");
                        if (!StringUtils.isEmpty(name)) {
                            value = name;
                        } else {
                            value = valueObj.toJSONString();
                        }
                    }
                    
                    // 处理日期类型
                    if (value instanceof Number) {
                        try {
                            long timestamp = ((Number) value).longValue();
                            if (timestamp > 10000000000L) {
                                Instant instant = Instant.ofEpochMilli(timestamp);
                                value = formatByDingDingFormatter(instant, null);
                            }
                        } catch (Exception e) {
                            // 保持原值
                        }
                    }
                    
                    // 转换为字符串（除了null）
                    rowData.put(englishFieldName, value == null ? null : String.valueOf(value));
                }
                
                // 2. 处理系统字段（从record元数据中获取）
                if (!CollectionUtils.isEmpty(systemFields)) {
                    // 创建人unionId
                    JSONObject createdBy = record.getJSONObject("createdBy");
                    String createdByUnionId = null;
                    if (createdBy != null) {
                        createdByUnionId = createdBy.getString("unionId");
                    }
                    if (systemFields.contains("created_by")) {
                        rowData.put("created_by", createdByUnionId);
                    }
                    
                    // 创建时间
                    if (systemFields.contains("created_time")) {
                        Long createdTime = record.getLong("createdTime");
                        if (createdTime != null) {
                            rowData.put("created_time", formatByDingDingFormatter(
                                    Instant.ofEpochMilli(createdTime), null));
                        }
                    }
                    
                    // 最近修改人unionId
                    JSONObject lastModifiedBy = record.getJSONObject("lastModifiedBy");
                    String lastModifiedByUnionId = null;
                    if (lastModifiedBy != null) {
                        lastModifiedByUnionId = lastModifiedBy.getString("unionId");
                    }
                    if (systemFields.contains("last_modified_by")) {
                        rowData.put("last_modified_by", lastModifiedByUnionId);
                    }
                    
                    // 最近修改时间
                    if (systemFields.contains("last_modified_time")) {
                        Long lastModifiedTime = record.getLong("lastModifiedTime");
                        if (lastModifiedTime != null) {
                            rowData.put("last_modified_time", formatByDingDingFormatter(
                                    Instant.ofEpochMilli(lastModifiedTime), null));
                        }
                    }
                    
                    // 最近修改人userId（通过unionId调用钉钉接口获取，使用缓存）
                    String lastModifiedUserId = null;
                    if (systemFields.contains("last_modified_user_id") && !StringUtils.isEmpty(lastModifiedByUnionId)) {
                        // 先查缓存
                        lastModifiedUserId = unionIdToUserIdCache.get(lastModifiedByUnionId);
                        if (StringUtils.isEmpty(lastModifiedUserId)) {
                            // 缓存中没有，调用钉钉接口
                            lastModifiedUserId = dingDingAiTableClient.getUserIdByUnionId(accessToken, lastModifiedByUnionId);
                            if (!StringUtils.isEmpty(lastModifiedUserId)) {
                                unionIdToUserIdCache.put(lastModifiedByUnionId, lastModifiedUserId);
                            }
                        }
                        rowData.put("last_modified_user_id", lastModifiedUserId);
                    }
                    
                    // 最近修改人name（通过userId调用钉钉接口获取，使用缓存）
                    if (systemFields.contains("last_modified_user_name") && !StringUtils.isEmpty(lastModifiedUserId)) {
                        // 先查缓存
                        String lastModifiedUserName = userIdToNameCache.get(lastModifiedUserId);
                        if (StringUtils.isEmpty(lastModifiedUserName)) {
                            // 缓存中没有，调用钉钉接口
                            lastModifiedUserName = dingDingAiTableClient.getUserNameByUserId(accessToken, lastModifiedUserId);
                            if (!StringUtils.isEmpty(lastModifiedUserName)) {
                                userIdToNameCache.put(lastModifiedUserId, lastModifiedUserName);
                            }
                        }
                        rowData.put("last_modified_user_name", lastModifiedUserName);
                    }
                }
                
                // 过滤空行
                if (!isEmptyRow(rowData)) {
                    allRecords.add(rowData);
                }
            }
            
            // 检查是否有下一页
            Boolean hasMore = response.getBoolean("hasMore");
            nextToken = hasMore != null && hasMore ? response.getString("nextToken") : null;
            
        } while (!StringUtils.isEmpty(nextToken));
        
        // 打印缓存统计
        log.warn("用户信息缓存统计 - unionId->userId缓存数: {}, userId->name缓存数: {}", 
                unionIdToUserIdCache.size(), userIdToNameCache.size());
        
        return allRecords;
    }
    
    /**
     * 批量插入记录
     * 
     * @param tableName 表名
     * @param fieldNames 字段名称列表
     * @param records 记录列表（Map形式）
     */
    private void batchInsertRecords(String tableName, List<String> fieldNames, List<Map<String, Object>> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        
        log.warn("开始批量插入数据，表名: {}, 字段数: {}, 记录数: {}", tableName, fieldNames.size(), records.size());
        
        // 分批插入（每批500条）
        int batchSize = 500;
        int totalBatches = (records.size() + batchSize - 1) / batchSize;
        
        for (int i = 0; i < totalBatches; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min((i + 1) * batchSize, records.size());
            List<Map<String, Object>> batch = records.subList(fromIndex, toIndex);
            
            try {
                int insertCount = dingDingTableSyncMapper.batchInsert(tableName, fieldNames, batch);
                log.warn("批量插入第{}/{}批，插入条数: {}", i + 1, totalBatches, insertCount);
            } catch (Exception e) {
                log.error("批量插入第{}批失败", i + 1, e);
                saveExceptionRecord(tableName, JSON.toJSONString(batch), e.getMessage());
            }
        }
    }
    
    /**
     * 判断是否为空行（所有字段都为空）
     * 
     * @param rowData 行数据（Map形式）
     * @return true-空行，false-非空行
     */
    private boolean isEmptyRow(Map<String, Object> rowData) {
        if (CollectionUtils.isEmpty(rowData)) {
            return true;
        }
        
        for (Object value : rowData.values()) {
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
            
            dingDingTableSyncMapper.insertExceptionRecord(type, jsonData, reason, tableName);
            log.warn("保存异常记录成功，表名: {}, 类型: {}", tableName, type);
        } catch (Exception e) {
            log.error("保存异常记录失败", e);
        }
    }
}
