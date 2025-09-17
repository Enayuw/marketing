package com.br.marketing.service.rulecenter.impl.push;

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
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.MarketingHaloCallbackRecordMapper;
import com.br.marketing.mapper.ScoreDorisLogMapper;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @ClassName HaloCallBackStrategy
 * @Author hang.zhou
 * @Date 2025/9/13
 */
@Service
public class HaloCallBackStrategy extends AbstractRuleCenterPushStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HaloCallBackStrategy.class);

    @Resource
    private ScoreDorisLogMapper scoreDorisLogMapper;

    @Resource
    private MarketingHaloCallbackRecordMapper haloCallbackRecordMapper;

    @Resource
    private FlagDataMapper flagDataMapper;

    /**
     * 预处理
     */
    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        logger.warn("{}开始执行推送策略预处理，任务ID: {}, 策略类型: {}", getPushName(context), context.getCustomerInfoPushMain().getId(), getPushName(context));

        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();

        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.equals(customerInfoPushMain.getmStatus())) {
            //执行补推逻辑
            retryProcess(customerInfoPushMain.getmApiCode(), customerInfoPushMain.getmCusBatchNumberList());
        }

        JSONObject haloAiCallbackConfig = marketingCommonConfig.getHaloAiCallbackConfig();
        String apiCodes = haloAiCallbackConfig.getString("apiCodes");
        String apiCode = context.getCustomerInfoPushMain().getmApiCode();
        if (!apiCodes.contains(apiCode)) {
            logger.warn("该apiCode未获得授权，请联系开发人员！apiCode:{}", apiCode);
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        String batchNumber = scoreDorisLogMapper.selectNewestBatchNumberLogbI_(apiCode);

        if (StringUtils.isBlank(batchNumber)) {
            String errMsg = "哈啰硅基人业务回调，无批次记录数据";
            logger.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        logger.warn("哈啰硅基人数据回传调度开始，batchNumber:{}", batchNumber);
        MarketingHaloCallbackRecordExample example = new MarketingHaloCallbackRecordExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBatchNumberEqualTo(batchNumber);
        List<MarketingHaloCallbackRecord> records = haloCallbackRecordMapper.selectByExample(example);
        if (!records.isEmpty() && HaloCallBackStatusEum.CALL_BACK_SUCCESS.equals(records.get(0).getStatus())) {
            logger.warn("该批次数据已处理完成: {}", batchNumber);
            new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
        }
        saveHaloCallbackRecord(batchNumber,apiCode);
        JSONObject haloSectionFieldConfig = marketingCommonConfig.getHaloSectionFieldConfig();
        String sectionField = haloSectionFieldConfig.get("sectionField").toString();
        JSONArray range = haloSectionFieldConfig.getJSONArray("range");
        //构造查询sql



        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
    }

    private Result<Boolean> retryProcess(String apiCode, String batchNumber) {
        MarketingHaloCallbackRecordExample example = new MarketingHaloCallbackRecordExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBatchNumberEqualTo(batchNumber);
        List<MarketingHaloCallbackRecord> records = haloCallbackRecordMapper.selectByExample(example);

        if (!records.isEmpty()) {
            MarketingHaloCallbackRecord record = records.get(0);
            if (HaloCallBackStatusEum.SYNCING.equals(record.getStatus())) {
                if (compareCounts(apiCode, batchNumber)) {
                    //todo 执行捞数回调

                } else {
                    return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
                }
            }
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
    }

    private void saveHaloCallbackRecord(String batchNumber, String apiCode) {
        MarketingHaloCallbackRecord haloCallbackRecord = new MarketingHaloCallbackRecord();
        haloCallbackRecord.setApiCode(apiCode);
        haloCallbackRecord.setBatchNumber(batchNumber);
        haloCallbackRecord.setStatus(0);
        haloCallbackRecordMapper.insertSelective(haloCallbackRecord);
    }

    private Boolean compareCounts(String apiCode, String batchNumber) {
        String countSql = String.format("select count(*) from b_marketing_score_%s where api_code = %s", batchNumber, apiCode);
        Long dorisCount = flagDataMapper.queryCountBySqlbI_(countSql);
        Long TIDBCount = flagDataMapper.queryCountBySql(countSql);
        return Objects.equals(dorisCount, TIDBCount);
    }

    private void createMarketingScoreTable(String batchNumber){
        List<Map<String, Object>> columnList = flagDataMapper.getTableColumnsbI_("b_score_".concat(batchNumber));
        List<String> baseColumnList = Arrays.asList("id","request_time","batch_number","cus_num","section","status","strategy_id","version","cell","userType");
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS b_marketing_score_");
        sql.append(batchNumber);
        sql.append("(");
        sql.append(" `id` bigint NOT NULL AUTO_INCREMENT COMMENT \"主键ID\",");
        sql.append(" `request_time` varchar(256) NULL,");
        sql.append(" `batch_number` varchar(256) NULL,");
        sql.append(" `cus_num` varchar(256) NULL,");
        sql.append(" `section` int NULL COMMENT \"分数段\",");
        sql.append(" `status` int NULL COMMENT \"状态\",");
        sql.append(" `strategy_id` varchar(256) NULL,");
        sql.append(" `version` varchar(256) NULL,");
        sql.append(" `cell` varchar(256) NULL,");
        sql.append(" `userType` varchar(256) NULL");
        // 过滤掉包含在baseColumnList中的列
        List<Map<String, Object>> filteredColumnList = columnList.stream()
                .filter(columnMap -> {
                    // 假设Map中的列名存储在"column_name"键中，根据实际情况调整
                    String columnName = (String) columnMap.get("column_name");
                    return !baseColumnList.contains(columnName);
                })
                .collect(Collectors.toList());

        sql.append(")");
        sql.append(" ENGINE=OLAP\n" +
                "UNIQUE KEY(`id`, `request_time`)\n" +
                "COMMENT 'OLAP'\n" +
                "DISTRIBUTED BY HASH(`id`) BUCKETS AUTO\n" +
                "PROPERTIES (\n" +
                "\"replication_allocation\" = \"tag.location.default: 1\",\n" +
                "\"min_load_replica_num\" = \"-1\",\n" +
                "\"is_being_synced\" = \"false\",\n" +
                "\"storage_medium\" = \"hdd\",\n" +
                "\"storage_format\" = \"V2\",\n" +
                "\"inverted_index_storage_format\" = \"V1\",\n" +
                "\"light_schema_change\" = \"true\",\n" +
                "\"disable_auto_compaction\" = \"false\",\n" +
                "\"enable_single_replica_compaction\" = \"false\",\n" +
                "\"group_commit_interval_ms\" = \"10000\",\n" +
                "\"group_commit_data_bytes\" = \"134217728\"\n" +
                ");");

    }

    /**
     * 构建查询sql
     * @param sectionField
     * @param range
     * @return
     */
    private String createSectionSql(String sectionField,JSONArray range) {
        return "";
    }

    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return null;
    }

    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return 0;
    }
}
