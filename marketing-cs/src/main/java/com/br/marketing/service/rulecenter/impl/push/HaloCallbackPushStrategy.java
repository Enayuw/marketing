package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.halo.HaluoAiApiServiceClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.MarketingRuleCenterHaloCallbackDataMapper;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName HaloCallbackPushStrategy
 * @Author hang.zhou
 * @Date 2025/9/17
 */
@Service
public class HaloCallbackPushStrategy extends AbstractRuleCenterPushStrategy {


    private static final Logger logger = LoggerFactory.getLogger(HaloCallbackPushStrategy.class);

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private HaluoAiApiServiceClient haluoAiApiServiceClient;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    private MarketingRuleCenterHaloCallbackDataMapper marketingRuleCenterHaloCallbackDataMapper;

    @Resource
    private FlagDataMapper flagDataMapper;

    private static final String TITLE = "【哈啰硅基人业务回调】";

    private static final String B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA = "b_marketing_rule_center_halo_callback_data";
    private static final String B_SCORE_PREFIX = "b_score_";

    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return new HaloCallbackPushTask(
                context.getPushThreadPool(),
                context.getCustomerInfoPushMain(),
                partitionIndex.toString());
    }

    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return 0;
    }

    protected Result<Boolean> validateData(RuleCenterPushContext context) {
        logger.warn(getPushName(context) + "开始执行推送策略数据校验，任务ID: {}, 策略类型: {}",
                context.getCustomerInfoPushMain().getId(),
                getPushName(context));

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        logger.warn("{}开始执行预处理，任务ID: {}", TITLE, customerInfoPushMain.getId());
        //根据taskId比较doris和tidb的量级，如果doris中存在数据且和tidb相等，同步完成，不再执行同步
        String countSql = "select count(1) from ".concat(B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA)
                .concat(" where m_id=").concat(String.valueOf(customerInfoPushMain.getId()));
        Long dorisCount = flagDataMapper.queryCountBySqlbI_(countSql);
        if (dorisCount > 0) {
            if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                    .equals(customerInfoPushMain.getmStatus())) {
                Long tiDbCount = flagDataMapper.queryCountBySql(countSql);
                if (dorisCount.equals(tiDbCount)) {
                    logger.warn("tidb同步完成");
                    // 回调重试

                    return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
                } else {
                    updatePushMainStatus(customerInfoPushMain.getId(), PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue());
                    return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
                }
            }
        }

        JSONObject haloAIRuleCenterCallbackConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
        String apiCode = customerInfoPushMain.getmApiCode();
        List<String> apiCodeList = Arrays.asList(haloAIRuleCenterCallbackConfig.getString("apiCodes").split(","));
        if (!apiCodeList.contains(apiCode)) {
            logger.warn("该apiCode未获得授权，请联系开发人员！apiCode:{}", apiCode);
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        String[] batchNumberList = customerInfoPushMain.getmCusBatchNumberList().split(",");
        String batchNumber = batchNumberList[0];

        //筛选数据入b_marketing_score_${batchNumber}表
        insertMarketingScoreTable(customerInfoPushMain.getmApiCode(), customerInfoPushMain.getId(), batchNumber);
        //同步TiDB
        syncDataToTiDB(customerInfoPushMain.getId());

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }


    /**
     * 哈啰硅基人回调任务实现类
     */
    @Data
    private class HaloCallbackPushTask implements Callable<List<Future<Result<Integer>>>> {

        private ThreadPoolExecutor pushCallbackPool;
        private CustomerInfoPushMain customerInfoPushMain;
        private String part;

        public HaloCallbackPushTask(ThreadPoolExecutor pushCallbackPool, CustomerInfoPushMain customerInfoPushMain, String part) {
            this.pushCallbackPool = pushCallbackPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.part = part;
        }

        @Override
        public List<Future<Result<Integer>>> call() {

            List<Future<Result<Integer>>> resultList = new ArrayList<>();
            Long taskId = customerInfoPushMain.getId();
            JSONObject haloAIRuleCenterCallbackConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
            int pageSize = haloAIRuleCenterCallbackConfig.getInteger("pageSize");
            long minId = 0L;
            int threadBatchSize = haloAIRuleCenterCallbackConfig.getInteger("threadBatchSize");

            while (true) {
                List<Map<String, Object>> results
                        = marketingRuleCenterHaloCallbackDataMapper.selectByTaskIdAndBatchNumber(taskId, minId, pageSize, 0);

                if (results.isEmpty()) {
                    logger.warn("当前任务数据已全部处理完成，taskId:{}", taskId);
                    break;
                }
                minId = ((Number) results.get(results.size() - 1).get("id")).longValue();

                for (List<Map<String, Object>> batchToProcess : Lists.partition(results, threadBatchSize)) {
                    List<Long> ids = batchToProcess.stream().map(record -> ((Number) record.get("id")).longValue()).collect(Collectors.toList());
                    // 从每个Map中移除id字段
                    batchToProcess.forEach(record -> record.remove("id"));
                    ReqHaluoApiDTO reqHaluoApiDTO = new ReqHaluoApiDTO();
                    reqHaluoApiDTO.setData(JSONObject.toJSONString(batchToProcess));
                    resultList.add(pushCallbackPool.submit(new CallbackTask(reqHaluoApiDTO, ids)));
                }
            }
            return resultList;
        }
    }

    private class CallbackTask implements Callable<Result<Integer>> {

        ReqHaluoApiDTO reqHaluoApiDTO;
        List<Long> ids;

        public CallbackTask(ReqHaluoApiDTO reqHaluoApiDTO, List<Long> ids) {
            this.reqHaluoApiDTO = reqHaluoApiDTO;
            this.ids = ids;
        }

        @Override
        public Result<Integer> call() throws Exception {
            Result<Integer> result = new Result<>();
            Result<String> flag = new Result<>();
            try {
                flag = haluoAiApiServiceClient.postHaluoCallbackApi(reqHaluoApiDTO);
                if (ResultCode.SUCCESS.getValue().equals(flag.getCode())) {
                    marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, 1);
                    result.setCode(ResultCode.SUCCESS.getValue());
                } else {
                    marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, 2);
                    result.setCode(ResultCode.FAIL.getValue());
                }
            } catch (Exception e) {
                marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, 2);
                String errMsg = "哈啰硅基人业务异常: " + e.getMessage();
                logger.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
                result.setCode(ResultCode.FAIL.getValue()).setMessage(flag.getMessage());
            }
            return result;
        }
    }

    private void updatePushMainStatus(Long id, Integer mStatus) {
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setId(id);
        main.setmStatus(mStatus);
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
    }

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
                    "insert into %s.marketing.b_marketing_rule_center_halo_callback_data (api_code,m_id,cus_num,cell,batch_number,status,section,extend)" +
                            " select api_code,m_id,cus_num,cell,batch_number,status,section,extend from %s.b_marketing_rule_center_halo_callback_data where m_id = %s",
                    syncDBName, fromDBName, id);

            logger.warn(TITLE + "执行同步SQL: {}", syncTiDBSql);
            flagDataMapper.insertbI_(syncTiDBSql);
            logger.warn(TITLE + "同步数据到Tidb明细表,耗时={}ms", System.currentTimeMillis() - start);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 筛选数据入Doris的b_marketing_rule_center_halo_callback_data表
     */
    private void insertMarketingScoreTable(String apiCode, Long id, String batchNumber) {
        List<String> baseColumnList = flagDataMapper.queryColumnNamebI_(B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA);
        List<String> columnList = flagDataMapper.queryColumnNamebI_(B_SCORE_PREFIX + batchNumber);
        JSONObject haloSectionFieldConfig = marketingCommonConfig.getHaloSectionFieldConfig();
        String sectionField = haloSectionFieldConfig.getString("sectionField");
        JSONArray rangeArray = haloSectionFieldConfig.getJSONArray("sectionRange");
        StringBuilder insertSql = new StringBuilder("INSERT INTO ").append(B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA).append("(");
        insertSql.append(String.join(",", baseColumnList));
        insertSql.append(")");
        insertSql.append("SELECT ");
        insertSql.append(apiCode).append(" as api_code,").append(id).append(" as m_id,");
        baseColumnList.remove("api_code");
        baseColumnList.remove("m_id");
        baseColumnList.remove("section");
        baseColumnList.remove("extend");
        baseColumnList.remove("status");
        insertSql.append(String.join(",", baseColumnList));
        insertSql.append(",");
        insertSql.append("0 as status,");

        // 生成CASE WHEN SQL和WHERE条件
        SectionSqlResult sectionResult = generateCaseWhenSql(sectionField, rangeArray);
        insertSql.append(sectionResult.getSectionSql());
        insertSql.append(",");

        columnList.removeAll(baseColumnList);
        StringBuilder extend = new StringBuilder("JSON_OBJECT(");
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
        insertSql.append(" FROM b_score_");
        insertSql.append(batchNumber);
        if (StringUtils.isNotBlank(sectionResult.getWhereSql())) {
            insertSql.append(" WHERE ");
            insertSql.append(sectionResult.getWhereSql());
        }

        flagDataMapper.insertbI_(insertSql.toString());
    }

    /**
     * 内部类用于返回section SQL和where条件
     */
    private static class SectionSqlResult {
        private final String sectionSql;
        private final String whereSql;

        public SectionSqlResult(String sectionSql, String whereSql) {
            this.sectionSql = sectionSql;
            this.whereSql = whereSql;
        }

        public String getSectionSql() {
            return sectionSql;
        }

        public String getWhereSql() {
            return whereSql;
        }
    }

    /**
     * 构建case-when语句
     *
     * @param sectionField 区间字段
     * @param rangeArray   区间
     * @return case-when语句
     */
    private SectionSqlResult generateCaseWhenSql(String sectionField, JSONArray rangeArray) {
        StringBuilder sql = new StringBuilder("CASE ");
        String whereSql = "";

        for (int i = 0; i < rangeArray.size(); i++) {
            JSONObject range = rangeArray.getJSONObject(i);
            String rangeStr = (String) range.get("range");
            Object value = range.get("value");

            String condition = parseRangeCondition(sectionField, rangeStr);
            if (i == 0) {
                // 提取第一个条件作为WHERE条件，去掉AND后面的部分
                whereSql = condition.replaceFirst("\\s+AND\\s+.*", "");
            }
            sql.append("WHEN ").append(condition).append(" THEN ").append(value).append(" ");
        }

        sql.append("ELSE NULL END AS section");
        return new SectionSqlResult(sql.toString(), whereSql);
    }


    /**
     * 解析区间生成条件
     *
     * @param sectionField 区间字段
     * @param rangeStr     区间
     * @return 条件
     */
    private String parseRangeCondition(String sectionField, String rangeStr) {
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
            condition.append(sectionField).append(" >= ").append(min);
        } else {
            condition.append(sectionField).append(" > ").append(min);
        }

        condition.append(" AND ");

        // 最大值条件
        if (maxInclusive) {
            condition.append(sectionField).append(" <= ").append(max);
        } else {
            condition.append(sectionField).append(" < ").append(max);
        }

        return condition.toString();
    }
}
