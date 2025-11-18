package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.halo.HaluoAiApiServiceClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.MarketingRuleCenterHaloCallbackDataExample;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.MarketingRuleCenterHaloCallbackDataMapper;
import com.br.marketing.service.halo.HaloRuleCenterCallbackService;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsConditionTransferSqlUtil;
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
    private HaloRuleCenterCallbackService haloRuleCenterCallbackService;

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
        MarketingRuleCenterHaloCallbackDataExample example = new MarketingRuleCenterHaloCallbackDataExample();
        example.createCriteria().andMIdEqualTo(customerInfoPushMain.getId()).andStatusEqualTo(2);
        Integer count = marketingRuleCenterHaloCallbackDataMapper.countByExample(example);
        if (count > 0) {
            return PushRuleStatusEnum.PUSH_FAIL.getValue();
        } else {
            return PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue();
        }
    }

    protected Result<Boolean> validateData(RuleCenterPushContext context) {
        logger.warn(getPushName(context) + "开始执行推送策略数据校验，任务ID: {}, 策略类型: {}",
                context.getCustomerInfoPushMain().getId(),
                getPushName(context));

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        try {
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
                        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
                    } else {
                        updatePushMainStatus(customerInfoPushMain.getId(), PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue());
                        return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
                    }
                }
            } else {
                JSONObject haloAIRuleCenterCallbackConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
                String apiCode = customerInfoPushMain.getmApiCode();
                List<String> apiCodeList = Arrays.asList(haloAIRuleCenterCallbackConfig.getString("apiCodes").split(","));
                if (!apiCodeList.contains(apiCode)) {
                    logger.warn("该apiCode未获得授权，请联系开发人员！apiCode:{}", apiCode);
                    return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
                }
                String[] batchNumberList = customerInfoPushMain.getmCusBatchNumberList().split(",");
                String batchNumber = batchNumberList[0];

                //筛选数据入b_marketing_rule_center_halo_callback_data表
                insertMarketingHaloCallbackTable(customerInfoPushMain.getmApiCode(), customerInfoPushMain.getId(), customerInfoPushMain.getmRuleCondition(), batchNumber);
                //同步TiDB
                syncDataToTiDB(customerInfoPushMain.getId());

                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
            }

        } catch (Exception e) {
            // 检查是否为超时异常
            if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("超时") || e.getMessage().contains("Communications link failure"))) {
                logger.error(TITLE + "前置处理超时异常，taskId: {}", customerInfoPushMain.getId(), e);
                updatePushMainStatus(customerInfoPushMain.getId(), PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue());
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE).setMessage("前置处理操作超时");
            } else {
                logger.error(TITLE + "前置处理异常，taskId: {}", customerInfoPushMain.getId(), e);
                updatePushMainStatus(customerInfoPushMain.getId(), PushRuleStatusEnum.PUSH_FAIL.getValue());
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE).setMessage("前置处理操作失败: " + e.getMessage());
            }
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
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
            return callback(pushCallbackPool, customerInfoPushMain, 0);
        }
    }

    List<Future<Result<Integer>>> callback(ThreadPoolExecutor pushCallbackPool, CustomerInfoPushMain customerInfoPushMain, Integer status) {
        List<Future<Result<Integer>>> resultList = new ArrayList<>();
        JSONObject haloAIRuleCenterCallbackConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
        int pageSize = haloAIRuleCenterCallbackConfig.getInteger("pageSize");
        long minId = 0L;
        int threadBatchSize = haloAIRuleCenterCallbackConfig.getInteger("threadBatchSize");
        String apiCode = customerInfoPushMain.getmApiCode();
        Long taskId = customerInfoPushMain.getId();
        while (true) {
            List<Map<String, Object>> results
                    = marketingRuleCenterHaloCallbackDataMapper.selectByTaskIdAndBatchNumber(apiCode, taskId, minId, pageSize, status);

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

                PushMarketingUserDTO<ReqHaluoApiDTO> pushMarketingUserDTO = new PushMarketingUserDTO<>();
                pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
                pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
                pushMarketingUserDTO.setJsonData(reqHaluoApiDTO);

                resultList.add(pushCallbackPool.submit(new CallbackTask(pushMarketingUserDTO, taskId, batchToProcess.size(), ids)));
            }
        }
        return resultList;
    }

    private class CallbackTask implements Callable<Result<Integer>> {

        PushMarketingUserDTO<ReqHaluoApiDTO> pushMarketingUserDTO;
        Long taskId;
        Integer size;
        List<Long> ids;

        public CallbackTask(PushMarketingUserDTO<ReqHaluoApiDTO> pushMarketingUserDTO, Long taskId, Integer size, List<Long> ids) {
            this.pushMarketingUserDTO = pushMarketingUserDTO;
            this.taskId = taskId;
            this.size = size;
            this.ids = ids;
        }

        @Override
        public Result<Integer> call() throws Exception {
            Result<Integer> result = new Result<>();
            Result<String> flag = new Result<>();
            try {
                // 模拟推决策异常
                HashMap<String, JSONObject> callbackSwitch = marketingCommonConfig.getCallbackSwitch();
                JSONObject mock = callbackSwitch.get(pushMarketingUserDTO.getApiCode());
                if (mock.get("switch") == Boolean.TRUE) {
                    logger.warn("{}进入挡板", TITLE);
                    long start = System.currentTimeMillis();
                    flag = callbackMessageMock(mock);
                    long end = System.currentTimeMillis();
                    logger.warn("{}结束挡板, result:{}, 耗时:{}", TITLE, flag, end - start);
                } else {
                    flag = haluoAiApiServiceClient.postHaluoCallbackApi(pushMarketingUserDTO.getJsonData());
                }

                if (ResultCode.SUCCESS.getValue().equals(flag.getCode())) {
                    marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, HaloCallbackStatusEnum.SUCCESS.getCode());
                    result.setCode(ResultCode.SUCCESS.getValue());
                } else {
                    marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, HaloCallbackStatusEnum.FAIL.getCode());
                    result.setCode(ResultCode.FAIL.getValue());
                }

            } catch (Exception e) {
                marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, HaloCallbackStatusEnum.FAIL.getCode());
                String errMsg = "哈啰硅基人业务异常: " + e.getMessage();
                logger.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
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
            // 检查是否为超时异常
            if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("超时") || e.getMessage().contains("Communications link failure"))) {
                logger.error(TITLE + "同步数据到TiDB超时异常，taskId: {}", id, e);
                throw new RuntimeException("同步数据到TiDB操作超时", e);
            } else {
                logger.error(TITLE + "同步数据到TiDB异常，taskId: {}", id, e);
                throw new RuntimeException("同步数据到TiDB操作失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 筛选数据入Doris的b_marketing_rule_center_halo_callback_data表
     */
    protected void insertMarketingHaloCallbackTable(String apiCode, Long id, String ruleCondition, String batchNumber) throws Exception {
        try {
            List<String> baseColumnList = flagDataMapper.queryColumnNamebI_(B_MARKETING_RULE_CENTER_HALO_CALLBACK_DATA);
            List<String> columnList = flagDataMapper.queryColumnNamebI_(B_SCORE_PREFIX + batchNumber);
            JSONObject haloSectionFieldConfig = marketingCommonConfig.getHaloAIRuleCenterCallbackConfig();
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
            insertSql.append(HaloCallbackStatusEnum.PENDING.getCode()).append(" as status,");

            // 生成CASE WHEN SQL和WHERE条件
            insertSql.append(generateCaseWhenSql(sectionField, rangeArray));
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

            //解析scoreCondition
            JSONObject ruleConditionObject = JSON.parseObject(ruleCondition);
            String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(ruleConditionObject, "");
            insertSql.append(" WHERE ");
            insertSql.append(sqlCondition);

            logger.warn("开始执行哈啰硅基人查询sql: {}", insertSql);
            flagDataMapper.insertbI_(insertSql.toString());
        } catch (Exception e) {
            // 检查是否为超时异常
            if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("超时") || e.getMessage().contains("Communications link failure"))) {
                logger.error(TITLE + "插入哈啰回调明细表超时异常，apiCode: {}, taskId: {}, batchNumber: {}", apiCode, id, batchNumber, e);
                throw new RuntimeException("插入哈啰回调明细表操作超时", e);
            } else {
                logger.error(TITLE + "插入哈啰回调明细表异常，apiCode: {}, taskId: {}, batchNumber: {}", apiCode, id, batchNumber, e);
                throw new RuntimeException("插入哈啰回调明细表操作失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 构建case-when语句
     *
     * @param sectionField 区间字段
     * @param rangeArray   区间
     * @return case-when语句
     */
    private String generateCaseWhenSql(String sectionField, JSONArray rangeArray) {
        StringBuilder sql = new StringBuilder("CASE ");

        for (int i = 0; i < rangeArray.size(); i++) {
            JSONObject range = rangeArray.getJSONObject(i);
            String rangeStr = (String) range.get("range");
            Object value = range.get("value");

            String condition = parseRangeCondition(sectionField, rangeStr);
            sql.append("WHEN ").append(condition).append(" THEN ").append(value).append(" ");
        }

        sql.append("ELSE NULL END AS section");
        return sql.toString();
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

    /**
     * 回调挡板
     *
     * @return
     */
    private Result<String> callbackMessageMock(Map<String, Object> mock) {
        Result<String> result = new Result<>();
        Integer code = (Integer) mock.get("code");
        if (ResultCode.SUCCESS.getValue().equals(code)) {
            result.setDate("");
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setMessage("");
            return result;
        }
        result.setCode(ResultCode.FAIL.getValue());
        result.setMessage("请求失败");
        return result;
    }

    @Override
    protected RuleCenterPushContext assemblePushContext(CustomerInfoPushMain customerInfoPushMain) {
        RuleCenterPushContext pushContext = super.assemblePushContext(customerInfoPushMain);
        pushContext.setPartitionCount(1);
        return pushContext;
    }

    @Override
    protected RuleCenterPushContext setThreadPoolNum(RuleCenterPushContext pushContext) {
        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;

        Integer getCallbackNum = marketingCommonConfig.getScoreToCallbackThreadNum() != null
                && marketingCommonConfig.getScoreToCallbackThreadNum() > 0
                ? marketingCommonConfig.getScoreToCallbackThreadNum()
                : 10;
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getCallbackNum, getCallbackNum, 50);
        pushContext.setEsThreadPool(actionEs);
        pushContext.setPushThreadPool(pushJc);
        return pushContext;
    }




}
