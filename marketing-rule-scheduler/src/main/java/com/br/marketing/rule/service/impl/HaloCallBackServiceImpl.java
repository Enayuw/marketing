package com.br.marketing.rule.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.MarketingHaloCallbackRecord;
import com.br.marketing.entity.MarketingHaloCallbackRecordExample;
import com.br.marketing.enums.HaloCallBackStatusEum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.MarketingHaloCallbackRecordMapper;
import com.br.marketing.mapper.ScoreDorisLogMapper;
import com.br.marketing.rule.service.HaloCallBackService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName HaloCallBackServiceImpl
 * @Author hang.zhou
 * @Date 2025/9/16
 */
@Service
public class HaloCallBackServiceImpl implements HaloCallBackService {

    private static final Logger logger = LoggerFactory.getLogger(HaloCallBackServiceImpl.class);

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    private ScoreDorisLogMapper scoreDorisLogMapper;

    @Resource
    private MarketingHaloCallbackRecordMapper haloCallbackRecordMapper;

    @Resource
    private FlagDataMapper flagDataMapper;

    private static final String TITLE = "【哈啰硅基人业务回调】";

    private static final String B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA = "b_marketing_rule_center_halo_callback_data";
    private static final String B_SCORE_PREFIX = "b_score_";

    @Override
    public Result<Boolean> callBack(Long id) {
        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        Result<Boolean> preProcessResult = preProcess(customerInfoPushMain);
        if (!preProcessResult.getCode().equals(ResultCode.SUCCESS.getValue())) {
            return preProcessResult;
        }

        String apiCode = customerInfoPushMain.getmApiCode();
        String batchNumber = customerInfoPushMain.getmCusBatchNumberList();
        Result<Boolean> doProcessResult = doProcess(apiCode, batchNumber);


        return null;
    }

    /**
     * 预处理，筛选符合条件的数据入b_marketing_score_${batchNumber}表，再同步到TiDB
     *
     * @param customerInfoPushMain 任务记录
     * @return 预处理结果
     */
    public Result<Boolean> preProcess(CustomerInfoPushMain customerInfoPushMain) {
        logger.warn("{}开始执行预处理，任务ID: {}", TITLE, customerInfoPushMain.getId());

        JSONObject haloAIRuleCenterCallbackConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
        String apiCode = customerInfoPushMain.getmApiCode();
        List<String> apiCodeList = Arrays.asList(haloAIRuleCenterCallbackConfig.getString("apiCodes").split(","));
        if (!apiCodeList.contains(apiCode)) {
            logger.warn("该apiCode未获得授权，请联系开发人员！apiCode:{}", apiCode);
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        String[] batchNUmberList = customerInfoPushMain.getmCusBatchNumberList().split(",");
        for (String batchNumber : batchNUmberList) {
            //筛选数据入b_marketing_score_${batchNumber}表
            insertMarketingScoreTable(customerInfoPushMain.getmApiCode(), customerInfoPushMain.getId(), batchNumber);
            //同步TiDB
            syncDataToTiDB(customerInfoPushMain.getId());
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);

    }

    /**
     * 执行回调逻辑
     *
     * @param apiCode     apiCode
     * @param batchNumber batchNumber
     * @return 回调结果
     */
    public Result<Boolean> doProcess(String apiCode, String batchNumber) {
        MarketingHaloCallbackRecordExample example = new MarketingHaloCallbackRecordExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBatchNumberEqualTo(batchNumber);
        List<MarketingHaloCallbackRecord> records = haloCallbackRecordMapper.selectByExample(example);

        if (!records.isEmpty()) {
            MarketingHaloCallbackRecord record = records.get(0);
            if (HaloCallBackStatusEum.CALL_BACK_SUCCESS.getCode().equals(record.getStatus())) {
                logger.warn("该批次数据已推送成功: {}", batchNumber);
                new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            } else if (HaloCallBackStatusEum.SYNCING.getCode().equals(record.getStatus())) {
                logger.warn("该批次数据正在同步中，batchNumber:{}", batchNumber);
                new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            } else if (HaloCallBackStatusEum.CALL_BACK_FAILURE.getCode().equals(record.getStatus())) {
                logger.warn("该批次数据推送失败，请联系开发人员，batchNumber:{}", batchNumber);
                new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            }
        }
        //不存在回调记录则创建新的的回调记录，初始状态为0（推送中）
        saveHaloCallbackRecord(apiCode, batchNumber);

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }


    /**
     * 新建回调记录
     *
     * @param apiCode     apiCode
     * @param batchNumber batchNumber
     */
    private void saveHaloCallbackRecord(String apiCode, String batchNumber) {
        MarketingHaloCallbackRecord haloCallbackRecord = new MarketingHaloCallbackRecord();
        haloCallbackRecord.setApiCode(apiCode);
        haloCallbackRecord.setBatchNumber(batchNumber);
        haloCallbackRecord.setStatus(0);
        haloCallbackRecordMapper.insertSelective(haloCallbackRecord);
    }

//    /**
//     * 创建b_marketing_score_${batchNumber}表
//     *
//     * @param batchNumber batchNumber
//     * @param columnList  用于保存所有字段
//     */
//    private void createMarketingScoreTable(String batchNumber, List<String> columnList) {
//        List<Map<String, Object>> columns = flagDataMapper.getTableColumnsbI_("b_score_".concat(batchNumber));
//        List<String> baseColumnList = Arrays.asList("id", "request_time", "batch_number", "cus_num", "section", "status", "strategy_id", "version", "cell", "userType");
//        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS b_marketing_score_");
//        sql.append(batchNumber);
//        sql.append("(");
//        sql.append(" `id` bigint NOT NULL AUTO_INCREMENT COMMENT \"主键ID\",");
//        sql.append(" `request_time` varchar(256) NULL,");
//        sql.append(" `batch_number` varchar(256) NULL,");
//        sql.append(" `cus_num` varchar(256) NULL,");
//        sql.append(" `section` int NULL COMMENT \"分数段\",");
//        sql.append(" `status` int NULL COMMENT \"状态\",");
//        sql.append(" `strategy_id` varchar(256) NULL,");
//        sql.append(" `version` varchar(256) NULL,");
//        sql.append(" `cell` varchar(256) NULL,");
//        sql.append(" `userType` varchar(256) NULL");
//        // 过滤掉包含在baseColumnList中的列
//        List<Map<String, Object>> filteredColumnList = columns.stream()
//                .filter(columnMap -> {
//                    // 假设Map中的列名存储在"column_name"键中，根据实际情况调整
//                    String columnName = (String) columnMap.get("columnName");
//                    return !baseColumnList.contains(columnName);
//                })
//                .collect(Collectors.toList());
//        columnList.addAll(baseColumnList);
//        columnList.addAll(filteredColumnList.stream()
//                .map(map -> map.get("columnName"))
//                .filter(Objects::nonNull) // 过滤 null 值
//                .map(Object::toString)    // 转换为 String
//                .collect(Collectors.toList()));
//        if (!filteredColumnList.isEmpty()) {
//            sql.append(",");
//            for (int i = 0; i < filteredColumnList.size(); i++) {
//                Map<String, Object> filteredColumn = filteredColumnList.get(i);
//                String columnName = (String) filteredColumn.get("columnName");
//                String dateType = (String) filteredColumn.get("dataType");
//                String nullable = filteredColumn.get("nullable").equals("YES") ? "NULL" : "NOT NULL";
//                if ("varchar".equals(dateType)) {
//                    Long maxLength = (Long) filteredColumn.get("maxLength");
//                    sql.append(" `").append(columnName).append("` ").append(dateType).append("(").append(maxLength).append(") ").append(nullable);
//                } else if ("decimal".equals(dateType)) {
//                    Long numericPrecision = (Long) filteredColumn.get("numericPrecision");
//                    Long numericScale = (Long) filteredColumn.get("numericScale");
//                    sql.append(" `").append(columnName).append("` ").append(dateType).append("(").append(numericPrecision).append(",").append(numericScale).append(") ").append(nullable);
//                }
//                if (i != filteredColumnList.size() - 1) {
//                    sql.append(",");
//                }
//            }
//        }
//        sql.append(")");
//        sql.append(" ENGINE=OLAP\n" +
//                "UNIQUE KEY(`id`, `request_time`)\n" +
//                "COMMENT 'OLAP'\n" +
//                "DISTRIBUTED BY HASH(`id`) BUCKETS AUTO\n" +
//                "PROPERTIES (\n" +
//                "\"replication_allocation\" = \"tag.location.default: 1\",\n" +
//                "\"min_load_replica_num\" = \"-1\",\n" +
//                "\"is_being_synced\" = \"false\",\n" +
//                "\"storage_medium\" = \"hdd\",\n" +
//                "\"storage_format\" = \"V2\",\n" +
//                "\"inverted_index_storage_format\" = \"V1\",\n" +
//                "\"light_schema_change\" = \"true\",\n" +
//                "\"disable_auto_compaction\" = \"false\",\n" +
//                "\"enable_single_replica_compaction\" = \"false\",\n" +
//                "\"group_commit_interval_ms\" = \"10000\",\n" +
//                "\"group_commit_data_bytes\" = \"134217728\"\n" +
//                ");");
//
//        try {
//            flagDataMapper.createTablebI_(sql.toString());
//        } catch (RuntimeException e) {
//            logger.error("{}创建b_marketing_score_{}", TITLE, batchNumber);
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * 同步数据到TiDB表
     */
    private void syncDataToTiDB(Long id) {
        try {
            long start = System.currentTimeMillis();

            // 检查配置是否为空
            if (marketingCommonConfig == null || marketingCommonConfig.getHaloAIRuleCenterCallbackConfig() == null) {
                logger.warn(TITLE + "配置信息为空，跳过同步数据到TiDB");
                return;
            }

            JSONObject pushPolicyConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
            String syncDBName = pushPolicyConfig.getString("syncDBName");
            String fromDBName = pushPolicyConfig.getString("fromDBName");

            // 检查必要的配置项
            if (StringUtils.isBlank(syncDBName) || StringUtils.isBlank(fromDBName)) {
                logger.warn(TITLE + "同步数据库配置项为空，syncDBName={}, fromDBName={}", syncDBName, fromDBName);
                return;
            }

            String refreshSql = "refresh catalog ".concat(syncDBName);
            flagDataMapper.insertbI_(refreshSql);

            String syncTiDBSql = String.format(
                    "insert into %s.marketing.b_marketing_rule_center_merge_push_data (api_code,m_id,cus_num,cell,batch_number,section,extend)" +
                            " select api_code,m_id,cus_num,cell,batch_number,section,extend from %s.b_marketing_rule_center_merge_push_data where m_id = %s",
                    syncDBName, fromDBName, id);

            logger.warn(TITLE + "执行同步SQL: {}", syncTiDBSql);
            flagDataMapper.insertbI_(syncTiDBSql);
            logger.warn(TITLE + "同步数据到Tidb明细表,耗时={}ms", System.currentTimeMillis() - start);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 筛选数据入b_marketing_rule_center_halo_callback_data表
     */
    private void insertMarketingScoreTable(String apiCode, Long id, String batchNumber) {
        List<String> baseColumnList = flagDataMapper.queryColumnNamebI_(B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA);
        List<String> columnList = flagDataMapper.queryColumnNamebI_(B_SCORE_PREFIX + batchNumber);
        JSONObject haloSectionFieldConfig = marketingCommonConfig.getHaloSectionFieldConfig();
        String sectionField = haloSectionFieldConfig.getString("sectionField");
        JSONArray rangeArray = haloSectionFieldConfig.getJSONArray("sectionRange");
        StringBuilder insertSql = new StringBuilder("INSERT INTO ").append(B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA).append("(");
        insertSql.append(apiCode).append(" as api_code,").append(id).append(" as m_id,");
        insertSql.append(String.join(",", baseColumnList));
        insertSql.append(")");
        insertSql.append("SELECT ");
        baseColumnList.remove("section");
        baseColumnList.remove("extend");
        insertSql.append(String.join(",", baseColumnList));
        insertSql.append(",");
        insertSql.append(generateCaseWhenSql(batchNumber, sectionField, rangeArray));
        insertSql.append(",");
        columnList.removeAll(baseColumnList);
        StringBuilder extend = new StringBuilder();
        List<String> extendFields = new ArrayList<>();
        for (String column : columnList) {
            extendFields.add("'" + column + "'");
            extendFields.add(column);
        }
        // 构建extend JSON对象
        if (!extendFields.isEmpty()) {
            extend.append(String.join(",", extendFields));
        }
        extend.append(") as extend");
        insertSql.append(extend);

        flagDataMapper.insertbI_(insertSql.toString());
    }

    private String generateCaseWhenSql(String batchNumber, String sectionField, JSONArray rangeArray) {
        StringBuilder sql = new StringBuilder("CASE ");
        String firstCondition = "";
        for (int i = 0; i < rangeArray.size(); i++) {
            JSONObject range = rangeArray.getJSONObject(i);
            String rangeStr = (String) range.get("range");
            Object value = range.get("value");

            String condition = parseRangeCondition(sectionField, rangeStr);
            if (i == 0) {
                firstCondition = condition.replaceFirst("\\s+AND\\s+.*", "");
            }
            sql.append("WHEN ").append(condition).append(" THEN ").append(value).append(" ");
        }

        sql.append("ELSE NULL END AS section");
        sql.append(" FROM b_score_");
        sql.append(batchNumber);
        sql.append(" WHERE ");
        sql.append(firstCondition);
        return sql.toString();
    }

    private String parseRangeCondition(String fieldName, String rangeStr) {
        // 解析区间字符串，如 "[40,45)" -> min=40, max=45, minInclusive=true, maxInclusive=false
        rangeStr = rangeStr.trim();
        char leftBracket = rangeStr.charAt(0);
        char rightBracket = rangeStr.charAt(rangeStr.length() - 1);

        String numbers = rangeStr.substring(1, rangeStr.length() - 1);
        String[] parts = numbers.split(",");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid range format: " + rangeStr);
        }

        double min = Double.parseDouble(parts[0]);
        double max = Double.parseDouble(parts[1]);

        boolean minInclusive = leftBracket == '[';
        boolean maxInclusive = rightBracket == ']';

        // 构建SQL条件
        StringBuilder condition = new StringBuilder();

        // 最小值条件
        if (minInclusive) {
            condition.append(fieldName).append(" >= ").append(min);
        } else {
            condition.append(fieldName).append(" > ").append(min);
        }

        condition.append(" AND ");

        // 最大值条件
        if (maxInclusive) {
            condition.append(fieldName).append(" <= ").append(max);
        } else {
            condition.append(fieldName).append(" < ").append(max);
        }

        return condition.toString();
    }
}
