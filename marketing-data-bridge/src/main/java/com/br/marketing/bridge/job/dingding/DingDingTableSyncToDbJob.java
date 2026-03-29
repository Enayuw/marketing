package com.br.marketing.bridge.job.dingding;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.dingding.aitable.DingDingAiTableClient;
import com.br.marketing.client.dingding.aitable.DingDingAiTableFieldDTO;
import com.br.marketing.client.dingding.aitable.DingDingAiTableFieldsResponse;
import com.br.marketing.client.dingding.aitable.DingDingAiTableRecordDTO;
import com.br.marketing.client.dingding.aitable.DingDingAiTableRecordsResponse;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
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

    public static final String QUOTE = "`";
    public static final String REGEX = "\\n";
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
            if (CollectionUtils.isEmpty(dingDingTableConfig)) {
                return;
            }

            for (Map.Entry<String, Object> entry : dingDingTableConfig.entrySet()) {
                String tableName = entry.getKey();
                JSONObject tableConfig = (JSONObject) entry.getValue();

                try {
                    syncTableData(tableName, tableConfig);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(),
                            "钉钉AI表格数据同步作业异常，tableName:" + tableName), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.warn("钉钉AI表格数据同步作业执行完成，耗时:{}ms", (endTime - startTime));
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(),
                    "钉钉AI表格数据同步作业异常"), e);
        }
    }

    /**
     * 同步单个表的数据
     * @param tableName   表名
     * @param tableConfig 表配置
     */
    private void syncTableData(String tableName, JSONObject tableConfig) {
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

        // 查询数据库表建表语句
        Map<String, Object> createTableResult = dingDingTableSyncMapper.getCreateTableSql(tableName);
        if (CollectionUtils.isEmpty(createTableResult)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "获取表{" + tableName + "}建表语句失败，跳过同步"
                    , "钉钉AI表格数据同步作业异常"));
            return;
        }

        String createTableSql = (String) createTableResult.get("Create Table");
        if (StringUtils.isEmpty(createTableSql)) {
            return;
        }

        // 中文 -> 英文
        Map<String, String> commentToFieldMap = new LinkedHashMap<>();
        // 业务字段（id后到created_by前）
        List<String> businessFields = new ArrayList<>();
        // 系统字段（created_by及之后）
        List<String> systemFields = new ArrayList<>();
        // 解析建表语句，获取字段和注释
        parseCreateTableSql(createTableSql, commentToFieldMap, businessFields, systemFields);

        // 获取AccessToken
        String accessToken = dingDingAiTableClient.getAccessToken(appKey, appSecret);
        if (StringUtils.isEmpty(accessToken)) {
            return;
        }

        // 获取钉钉表格字段信息
        Result<DingDingAiTableFieldsResponse> fieldsResult = dingDingAiTableClient.getSheetFields(
                accessToken, baseId, sheetId, operatorId);

        // 检查调用结果，最终失败后触发报警
        if (fieldsResult == null || !ResultCode.SUCCESS.getValue().equals(fieldsResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "获取钉钉表格字段失败，表名:" + tableName + ", code: " + (fieldsResult != null ? fieldsResult.getCode() : "null") +
                            ", message: " + (fieldsResult != null ? fieldsResult.getMessage() : "返回结果为空")
                    , "钉钉AI表格数据同步作业异常"));
            return;
        }

        DingDingAiTableFieldsResponse fieldsResponse = fieldsResult.getData();
        // 构建字段名到formatter的映射
        Map<String, String> fieldFormatterMap = new HashMap<>();
        if (fieldsResponse != null && !CollectionUtils.isEmpty(fieldsResponse.getValue())) {
            for (DingDingAiTableFieldDTO field : fieldsResponse.getValue()) {
                String formatter = field.getFormatter();
                if (!StringUtils.isEmpty(formatter)) {
                    fieldFormatterMap.put(field.getName(), formatter);
                }
            }
        }

        // 获取钉钉数据记录
        List<String> allFieldNames = new ArrayList<>(businessFields);
        allFieldNames.addAll(systemFields);
        List<Map<String, Object>> allRecords = fetchAllRecordsWithMapping(
                accessToken, baseId, sheetId, operatorId, commentToFieldMap,
                businessFields, fieldFormatterMap);

        if (CollectionUtils.isEmpty(allRecords)) {
            return;
        }

        log.warn("表{}获取到{}条数据记录", tableName, allRecords.size());

        // 删除旧数据
        int deleteCount = dingDingTableSyncMapper.deleteAll(tableName);
        log.warn("删除表{}旧数据，删除条数: {}", tableName, deleteCount);

        // 批量插入新数据
        batchInsertRecords(tableName, allFieldNames, allRecords);

        log.warn("表{}数据同步完成，插入条数: {}", tableName, allRecords.size());
    }

    /**
     * 解析建表语句，提取字段和注释
     * @param createTableSql    建表语句
     * @param commentToFieldMap 中文注释到英文字段名的映射（输出）
     * @param businessFields    业务字段列表（输出）
     * @param systemFields      系统字段列表（输出）
     */
    private void parseCreateTableSql(String createTableSql, Map<String, String> commentToFieldMap,
                                     List<String> businessFields, List<String> systemFields) {
        // 定义系统字段集合（固定的）
        Set<String> systemFieldSet = new HashSet<>(Arrays.asList(
                "created_by", "created_time",
                "last_modified_by", "last_modified_time",
                "last_modified_user_id", "last_modified_user_name"
        ));

        // 按行分割建表语句
        String[] lines = createTableSql.split(REGEX);

        for (String line : lines) {
            line = line.trim();

            // 只处理字段定义行（以`开头）
            if (!line.startsWith(QUOTE)) {
                continue;
            }

            // 提取字段名：`字段名`
            int endPos = line.indexOf(QUOTE, 1);
            if (endPos == -1) {
                continue;
            }

            String fieldName = line.substring(1, endPos);

            // 跳过id字段（自增主键）
            if ("id".equalsIgnoreCase(fieldName)) {
                continue;
            }

            // 提取注释：comment '注释内容'
            String fieldComment = null;
            int commentIdx = line.toLowerCase().indexOf("comment '");
            if (commentIdx != -1) {
                int start = commentIdx + 9;
                int end = line.indexOf('\'', start);
                if (end != -1) {
                    fieldComment = line.substring(start, end).trim();
                }
            }

            // 判断是系统字段还是业务字段
            if (systemFieldSet.contains(fieldName.toLowerCase())) {
                systemFields.add(fieldName);
            } else {
                businessFields.add(fieldName);
                if (!StringUtils.isEmpty(fieldComment)) {
                    commentToFieldMap.put(fieldComment, fieldName);
                }
            }
        }

        log.warn("解析完成 - 业务字段: {}, 系统字段: {}, 中文映射: {}",
                businessFields, systemFields, commentToFieldMap);
    }

    /**
     * 获取所有数据记录（分页查询，带字段映射）
     * @param accessToken       访问令牌
     * @param baseId            Base ID
     * @param sheetId           Sheet ID
     * @param operatorId        操作人ID
     * @param commentToFieldMap 中文列名到英文字段名的映射
     * @param businessFields    业务字段列表
     * @param fieldFormatterMap 字段formatter映射（中文列名->formatter）
     * @return 所有记录（Map形式）
     */
    private List<Map<String, Object>> fetchAllRecordsWithMapping(String accessToken, String baseId, String sheetId,
                                                                 String operatorId, Map<String, String> commentToFieldMap,
                                                                 List<String> businessFields,
                                                                 Map<String, String> fieldFormatterMap) {
        List<Map<String, Object>> allRecords = new ArrayList<>();
        String nextToken = null;
        int pageNum = 0;

        // 用户信息缓存，避免重复调用钉钉接口
        Map<String, String> unionIdToUserIdCache = new HashMap<>();  // unionId -> userId
        Map<String, String> userIdToNameCache = new HashMap<>();     // userId -> userName

        do {
            pageNum++;
            Result<DingDingAiTableRecordsResponse> recordsResult = dingDingAiTableClient.getSheetRecords(
                    accessToken, baseId, sheetId, operatorId, nextToken, 100);

            // 检查调用结果，最终失败后触发报警
            if (recordsResult == null || !ResultCode.SUCCESS.getValue().equals(recordsResult.getCode())) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "获取钉钉表格数据失败，第" + pageNum + "页, code: " + (recordsResult != null ? recordsResult.getCode() : "null") +
                                ", message: " + (recordsResult != null ? recordsResult.getMessage() : "返回结果为空")
                        , "钉钉AI表格数据同步作业异常"));
                break;
            }

            DingDingAiTableRecordsResponse response = recordsResult.getData();
            if (response == null || CollectionUtils.isEmpty(response.getRecords())) {
                break;
            }

            log.warn("获取第{}页数据，记录数: {}", pageNum, response.getRecords().size());

            // 解析每条记录
            for (DingDingAiTableRecordDTO record : response.getRecords()) {
                JSONObject fieldsData = record.getFields();

                // 映射数据：中文列名 -> 英文字段名
                Map<String, Object> rowData = new LinkedHashMap<>();

                // 1. 处理业务字段（从fields中用中文列名取值）
                for (Map.Entry<String, String> entry : commentToFieldMap.entrySet()) {
                    String chineseColumnName = entry.getKey();  // 中文列名（COMMENT）
                    String englishFieldName = entry.getValue(); // 英文字段名

                    Object value = fieldsData.get(chineseColumnName);

                    // 取name值
                    if (value instanceof JSONObject) {
                        JSONObject valueObj = (JSONObject) value;
                        String name = valueObj.getString("name");
                        value = !StringUtils.isEmpty(name) ? name : null;
                    }

                    // 处理日期类型（如果钉钉返回了formatter，则按formatter转换）
                    String formatter = fieldFormatterMap.get(chineseColumnName);
                    if (!StringUtils.isEmpty(formatter) && value instanceof Number) {
                        try {
                            long timestamp = ((Number) value).longValue();
                            Instant instant = Instant.ofEpochMilli(timestamp);
                            value = formatByDingDingFormatter(instant, formatter);
                        } catch (Exception e) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                                    "格式化日期字段异常，chineseColumnName:" + chineseColumnName + e.getMessage()
                                    , "钉钉AI表格数据同步作业异常"), e);
                        }
                    }

                    // 转换为字符串并trim
                    String strValue = value == null ? null : String.valueOf(value).trim();
                    rowData.put(englishFieldName, strValue);
                }

                // 2. 处理系统字段（从record元数据中获取）
                // 获取创建人和修改人的unionId
                JSONObject createdBy = record.getCreatedBy();
                JSONObject lastModifiedBy = record.getLastModifiedBy();

                String createdByUnionId = createdBy != null ? createdBy.getString("unionId") : null;
                String lastModifiedByUnionId = lastModifiedBy != null ? lastModifiedBy.getString("unionId") : null;

                // 填充系统字段
                rowData.put("created_by", createdByUnionId);

                rowData.put("created_time", record.getCreatedTime() != null ?
                        formatByDingDingFormatter(Instant.ofEpochMilli(record.getCreatedTime()), null) : null);

                rowData.put("last_modified_by", lastModifiedByUnionId);

                rowData.put("last_modified_time", record.getLastModifiedTime() != null ?
                        formatByDingDingFormatter(Instant.ofEpochMilli(record.getLastModifiedTime()), null) : null);

                // 获取userId和userName（使用缓存优化）
                String userId;
                // 先查缓存
                userId = unionIdToUserIdCache.get(lastModifiedByUnionId);
                if (StringUtils.isEmpty(userId)) {
                    userId = dingDingAiTableClient.getUserIdByUnionId(accessToken, lastModifiedByUnionId);
                    if (!StringUtils.isEmpty(userId)) {
                        unionIdToUserIdCache.put(lastModifiedByUnionId, userId);
                    }
                }

                rowData.put("last_modified_user_id", userId);

                String userName = userIdToNameCache.get(userId);
                if (StringUtils.isEmpty(userName)) {
                    userName = dingDingAiTableClient.getUserNameByUserId(accessToken, userId);
                    if (!StringUtils.isEmpty(userName)) {
                        userIdToNameCache.put(userId, userName);
                    }
                }
                rowData.put("last_modified_user_name", userName);

                // 过滤空行（只判断业务字段是否为空）
                if (!isEmptyRow(rowData, businessFields)) {
                    allRecords.add(rowData);
                }
            }

            // 检查是否有下一页
            nextToken = response.getHasMore() != null && response.getHasMore() ?
                    response.getNextToken() : null;

        } while (!StringUtils.isEmpty(nextToken));

        log.warn("用户信息缓存统计 - unionId->userId缓存数: {}, userId->name缓存数: {}",
                unionIdToUserIdCache.size(), userIdToNameCache.size());

        return allRecords;
    }

    /**
     * 批量插入记录（使用MyBatis批量插入，每2000条一批）
     * @param tableName  表名
     * @param fieldNames 字段名称列表
     * @param records    记录列表（Map形式）
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
                // 将Map转换为值列表（按fieldNames顺序）
                List<List<Object>> valuesList = new ArrayList<>();
                for (Map<String, Object> record : batch) {
                    List<Object> values = new ArrayList<>();
                    for (String fieldName : fieldNames) {
                        values.add(record.get(fieldName));
                    }
                    valuesList.add(values);
                }

                int insertCount = dingDingTableSyncMapper.batchInsertByValues(tableName, fieldNames, valuesList);

                log.warn("批量插入第{}/{}批，插入条数: {}", i + 1, totalBatches, insertCount);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "批量写入数据异常" + e.getMessage()
                        , "钉钉AI表格数据同步作业异常"), e);
            }
        }
    }

    /**
     * 判断是否为空行（只判断业务字段是否都为空）
     * @param rowData        行数据（Map形式）
     * @param businessFields 业务字段列表
     * @return true-空行，false-非空行
     */
    private boolean isEmptyRow(Map<String, Object> rowData, List<String> businessFields) {
        if (CollectionUtils.isEmpty(rowData)) {
            return true;
        }

        // 只判断业务字段是否都为空
        for (String businessField : businessFields) {
            Object value = rowData.get(businessField);
            if (value != null && !StringUtils.isEmpty(String.valueOf(value).trim())) {
                return false;  // 只要有一个业务字段不为空，就不是空行
            }
        }

        return true;  // 所有业务字段都为空
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
}
