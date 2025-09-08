package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.log.AlertLog;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.bean.ScoreLable;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ErrorMark;
import com.br.marketing.entity.MarketingRuleCenterMergePushData;
import com.br.marketing.enums.*;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.mapper.ErrorMarkMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.MarketingRuleCenterMergePushDataMapper;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.util.GeneScriptUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName MergeDataPushStrategy
 * @Author hang.zhou
 * @Date 2025/9/1
 */
@Service
@Slf4j
public class MergeDataPushStrategy extends AbstractRuleCenterPushStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MergeDataPushStrategy.class);

    public static final String TITLE = "[合并数据推送决策]";
    
    // 常量定义
    private static final String B_MARKETING_RULE_CENTER_MERGE_PUSH_DATA = "b_marketing_rule_center_merge_push_data";
    private static final String B_SCORE_PREFIX = "b_score_";
    private static final String CASE_ADD_METHOD = "caseAdd";
    private static final int DEFAULT_PAGE_SIZE = 2000;

    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Resource
    private FlagDataMapper flagDataMapper;

    @Resource
    private RuleCenterLabelService ruleCenterLabelService;

    @Resource
    private MarketingRuleCenterMergePushDataMapper marketingRuleCenterMergePushDataMapper;

    @Autowired
    private IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Resource
    private ErrorMarkMapper errorMarkMapper;

    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return new MergePushPolicyTask(
                context.getPushThreadPool(),
                context.getCustomerInfoPushMain(),
                context.getMarkWithEsFlag(),
                context.getEncryptType(),
                context.getLabelObject()
        );
    }

    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                FilterTypeEnum.GENERAL_POLICY.getValue());
    }

    protected Result<Boolean> validateData(RuleCenterPushContext context) {
        logger.warn(getPushName(context) + "开始执行推送策略数据校验，任务ID: {}, 策略类型: {}",
                context.getCustomerInfoPushMain().getId(),
                getPushName(context));
        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        //加密校验
        Result<Integer> integerResult = pushRuleService.checkThreekEnc(context.getFileIds());
        if (!ResultCode.SUCCESS.getValue().equals(integerResult.getCode())) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    String.format("该推送不符合推送决策的限制条件 流水号：%s,原因：%s", pushMain.getId().toString(), integerResult.getMessage())));
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        //赋值加密方式
        context.setEncryptType(integerResult.getData());

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
    }

    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        try {
            CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
            String cusBatchNumberString = customerInfoPushMain.getmCusBatchNumberList();
            String[] cusBatchNumberList = cusBatchNumberString.split(",");
            Set<String> unionColumns = new HashSet<>();
            for (String cusBatchNumber : cusBatchNumberList) {
                List<String> columnList = flagDataMapper.queryColumnNamebI_(B_SCORE_PREFIX + cusBatchNumber);
                unionColumns.addAll(columnList);
            }
            String originalSelect = StringUtils.join(unionColumns, ",");
            //调用方法拼接sql，多表情况下字段取哪张表
            List<String> batchNumberList = Arrays.asList(customerInfoPushMain.getmCusBatchNumberList().split(","));
            String processSelect = ruleCenterLabelService.scoreMergeFieldMapping(originalSelect, batchNumberList, customerInfoPushMain.getmApiCode());

            PushCustomerDTO pushCustomerDTO = new PushCustomerDTO();
            pushCustomerDTO.setmRuleCondition(customerInfoPushMain.getmRuleCondition());
            pushCustomerDTO.setBatchNumberList(batchNumberList);
            pushCustomerDTO.setScoreMergeField(customerInfoPushMain.getExtend());
            pushCustomerDTO.setApiCode(customerInfoPushMain.getmApiCode());
            String processFrom = ruleCenterLabelService.scoreMergeAssemble(pushCustomerDTO);

            //基础字段列表
            List<String> baseColumns = flagDataMapper.queryColumnNamebI_(B_MARKETING_RULE_CENTER_MERGE_PUSH_DATA);
            String finalSelect = (customerInfoPushMain.getmApiCode() + " as api_code," + customerInfoPushMain.getId() + " as m_id, ")
                    .concat(generateSelectColumns(processSelect, baseColumns));
            String selectSql = "select ".concat(finalSelect).concat(" ").concat(processFrom);

            baseColumns.remove("id");
            String insertColumn = String.join(",", baseColumns);
            String insertDorisSql = "insert into ".concat(B_MARKETING_RULE_CENTER_MERGE_PUSH_DATA).concat("(").concat(insertColumn).concat(")").concat(selectSql);
            flagDataMapper.insertbI_(insertDorisSql);

            //同步tidb
            syncDataToTiDB();

            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("{}前置处理异常", TITLE, e);
        }
        return null;
    }


    /**
     * 合并数据推决策任务实现类
     */
    @Data
    private class MergePushPolicyTask implements Callable<List<Future<Result<Integer>>>> {

        private ThreadPoolExecutor pushJcPool;
        private CustomerInfoPushMain customerInfoPushMain;
        private Boolean markWithEsFlag;
        private Integer _3kEncrypt;
        private Object lableObject;

        public MergePushPolicyTask(ThreadPoolExecutor pushJcPool, CustomerInfoPushMain customerInfoPushMain, Boolean markWithEsFlag, Integer _3kEncrypt, Object lableObject) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.markWithEsFlag = markWithEsFlag;
            this._3kEncrypt = _3kEncrypt;
            this.lableObject = lableObject;
        }

        @Override
        public List<Future<Result<Integer>>> call() {
            // 初始化评分标签处理
            boolean hasScoreLabel = !ObjectUtils.isEmpty(lableObject);
            List<ScoreLable> scoreLables = null;
            if (hasScoreLabel && !markWithEsFlag) {
                scoreLables = (List<ScoreLable>) lableObject;
            }

            // 分页查询
            Long taskId = customerInfoPushMain.getId();
            Integer pageSize = DEFAULT_PAGE_SIZE;
            Long minId = null;
            List<Future<Result<Integer>>> resultList = new ArrayList<>();

            while (true) {
                List<MarketingRuleCenterMergePushData> marketingRuleCenterMergePushDataList =
                        marketingRuleCenterMergePushDataMapper.selectByTaskId(taskId, pageSize, minId);
                if (marketingRuleCenterMergePushDataList.isEmpty()) {
                    break;
                }
                minId = marketingRuleCenterMergePushDataList.get(marketingRuleCenterMergePushDataList.size() - 1).getId();

                int realNum = marketingRuleCenterMergePushDataList.size();
                logger.warn("任务id：{}，获取的数量：{}"
                        , customerInfoPushMain.getId()
                        , realNum);

                List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
                for (MarketingRuleCenterMergePushData marketingRuleCenterMergePushData : marketingRuleCenterMergePushDataList) {
                    //人员信息
                    PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
                    if (logger.isInfoEnabled()) {
                        logger.info("人员信息：cusnum:{};batchnumber:{}", marketingRuleCenterMergePushData.getCusNum(),
                                (StringUtils.isNotBlank(marketingRuleCenterMergePushData.getBatchNumber()) ? marketingRuleCenterMergePushData.getBatchNumber() : ""));
                    }
                    dto1.setCaseNumber(marketingRuleCenterMergePushData.getCusNum());
                    dto1.setPhone(encrypt3k(_3kEncrypt, marketingRuleCenterMergePushData.getCell()));
                    JSONObject varObject = JSON.parseObject(marketingRuleCenterMergePushData.getExtend());
                    if (varObject == null) {
                        varObject = new JSONObject();
                    }
                    //构建conditions
                    List<MarketingCondition> conditions = new ArrayList<>();
                    Set<Map.Entry<String, Object>> entrySet = varObject.entrySet();
                    for (Map.Entry<String, Object> entry : entrySet) {
                        MarketingCondition marketingCondition = new MarketingCondition();
                        marketingCondition.setFieldKey(entry.getKey());
                        String strValue = JSON.toJSONString(entry.getValue());
                        marketingCondition.setStrValue(strValue);
                        if (NumberUtils.isNumber(strValue)) {
                            marketingCondition.setDValue(Double.valueOf(strValue));
                        }
                        conditions.add(marketingCondition);
                    }

                    varObject.put("custNum", marketingRuleCenterMergePushData.getCusNum());
                    varObject.put("idCard", encrypt3k(_3kEncrypt, marketingRuleCenterMergePushData.getIdCard()));
                    varObject.put("name", encrypt3k(_3kEncrypt, marketingRuleCenterMergePushData.getName()));
                    varObject.put("batchNumber", marketingRuleCenterMergePushData.getBatchNumber());
                    varObject.put("taskId", marketingRuleCenterMergePushData.getmId());
                    varObject.put("userType", marketingRuleCenterMergePushData.getUserType());

                    if (hasScoreLabel) {
                        if (!CollectionUtils.isEmpty(conditions)) {
                            Map<String, Object> scoreMap = conditions.stream()
                                    .filter(condition -> condition.getDValue() != null)
                                    .collect(Collectors.toMap(MarketingCondition::getFieldKey
                                            , MarketingCondition::getDValue
                                            , (existing, replacement) -> replacement));
                            ScoreLable scoreLable = GeneScriptUtil.scoreLableWithSpel(scoreMap, scoreLables);
                            if (scoreLable != null) {
                                varObject.put("listValue", scoreLable.getListValue());
                                varObject.put("valueType", scoreLable.getValueType());
                            }
                        }
                    }

                    dto1.setVariables(varObject);
                    if (StringUtils.isNotBlank(customerInfoPushMain.getStrategyCode())) {
                        dto1.setStrategyCode(customerInfoPushMain.getStrategyCode());
                    }
                    userDetailDTOS.add(dto1);
                }

                //推送任务基础信息
                List<List<PushMarketingUserDetailDTO>> partition =
                        toPolicyByRuleService.splitParam(customerInfoPushMain.getmApiCode(), userDetailDTOS);
                Integer batch = 0;
                for (List<PushMarketingUserDetailDTO> userDetailDTOList : partition) {
                    PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                    pushMarketingUserTaskInfoDTO.setMethod(CASE_ADD_METHOD);
                    pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
                    pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId().toString() + "_" + batch);
                    pushMarketingUserTaskInfoDTO.setData(userDetailDTOList);
                    pushMarketingUserTaskInfoDTO.setTaskId(customerInfoPushMain.getId().toString());
                    pushMarketingUserTaskInfoDTO.setBatchName(customerInfoPushMain.getBatchName());
                    if (StringUtils.isNotBlank(customerInfoPushMain.getStrategyCode())) {
                        pushMarketingUserTaskInfoDTO.setStrategyCode(customerInfoPushMain.getStrategyCode());
                    }
                    //传输参数信息
                    PushMarketingUserDTO<PushMarketingUserTaskInfoDTO> pushMarketingUserDTO = new PushMarketingUserDTO<>();
                    pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
                    pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
                    pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);
                    resultList.add(pushJcPool.submit(new MergeDataPushStrategy.PushJcAction(pushMarketingUserDTO,
                            pushMarketingUserTaskInfoDTO.getAccessNumber(),
                            customerInfoPushMain.getId(),
                            userDetailDTOList.size())));
                    batch++;
                }


            }
            return resultList;
        }
    }


    /**
     * 同步数据到TiDB表
     */
    private void syncDataToTiDB() {
        try {
            long start = System.currentTimeMillis();

            // 检查配置是否为空
            if (marketingCommonConfig == null || marketingCommonConfig.getPushPolicyConfig() == null) {
                logger.warn(TITLE + "配置信息为空，跳过同步数据到TiDB");
                return;
            }

            Map<String, String> pushPolicyConfig = marketingCommonConfig.getPushPolicyConfig();
            String syncDBName = pushPolicyConfig.get("syncDBName");
            String fromDBName = pushPolicyConfig.get("fromDBName");

            // 检查必要的配置项
            if (StringUtils.isBlank(syncDBName) || StringUtils.isBlank(fromDBName)) {
                logger.warn(TITLE + "同步数据库配置项为空，syncDBName={}, fromDBName={}", syncDBName, fromDBName);
                return;
            }

            String refreshSql = "refresh catalog ".concat(syncDBName);
            flagDataMapper.insertbI_(refreshSql);

            String syncTiDBSql = String.format(
                    "insert into %s.marketing.b_marketing_rule_center_merge_push_data (api_code,m_id,cus_num,cell,id_card,user_type,name,batch_number,extend) " +
                            "select api_code,m_id,cus_num,cell,id_card,user_type,name,batch_number,extend from %s.b_marketing_rule_center_merge_push_data",
                    syncDBName, fromDBName);

            logger.warn(TITLE + "执行同步SQL: {}", syncTiDBSql);
            flagDataMapper.insertbI_(syncTiDBSql);
            logger.warn(TITLE + "同步数据到Tidb明细表,耗时={}ms", System.currentTimeMillis() - start);

        } catch (Exception e) {
            logger.error(TITLE + "同步数据到TiDB异常", e);
            // 不抛出异常，避免影响主流程
        }
    }


    /**
     * 组装select字段
     *
     * @param originalString 原始字段
     * @param baseColumns    基础字段
     */
    public static String generateSelectColumns(String originalString, List<String> baseColumns) {
        List<String> allFields = Arrays.stream(originalString.split(","))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        // 建立字段名到完整字段表达式的映射
        Map<String, String> fieldMap = new HashMap<>();
        for (String field : allFields) {
            String fieldName = extractFieldName(field);
            // 处理字段名映射
            if (fieldName.equals("userType")) {
                fieldMap.put("user_type", field);
            } else if (fieldName.equals("id")) {
                fieldMap.put("id_card", field);
            } else {
                fieldMap.put(fieldName, field);
            }
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder extend = new StringBuilder("JSON_OBJECT(");
        List<String> extendFields = new ArrayList<>();

        // 按照baseColumns的顺序拼接字段
        for (String baseColumn : baseColumns) {
            if (fieldMap.containsKey(baseColumn)) {
                String fieldExpression = fieldMap.get(baseColumn);
                columns.append(fieldExpression).append(" as ").append(baseColumn).append(",");
                fieldMap.remove(baseColumn); // 移除已处理的字段
            }
        }

        // 处理剩余不在baseColumns中的字段，添加到extend JSON对象中
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String originalFieldName = extractFieldName(entry.getValue());
            extendFields.add("'" + originalFieldName + "'");
            extendFields.add(entry.getValue());
        }

        // 构建extend JSON对象
        if (!extendFields.isEmpty()) {
            extend.append(String.join(",", extendFields));
        }
        extend.append(") as extend");

        return columns.append(extend).toString();
    }

    /**
     * 去除前缀
     *
     * @param fullField 前缀.字段名
     */
    private static String extractFieldName(String fullField) {
        if (fullField.contains(".")) {
            return fullField.split("\\.")[1];
        }
        return fullField;
    }

    public String encrypt3k(Integer type, String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }
        if (ScoreThreeKeyEncryptEnum.md5.getValue().equals(type)) {
            return DigestUtils.md5DigestAsHex(content.getBytes());
        }
        if (ScoreThreeKeyEncryptEnum.sha256.getValue().equals(type)) {
            return Sha256Util.getSHA256Encrypt(content);
        }
        return content;
    }


    /**
     * 推送决策Action -
     */
    class PushJcAction implements Callable<Result<Integer>> {

        private PushMarketingUserDTO<PushMarketingUserTaskInfoDTO> pushMarketingUserDTO;
        private String accessNumber;
        private Long mainId;
        private Integer size;

        public PushJcAction(PushMarketingUserDTO<PushMarketingUserTaskInfoDTO> pushMarketingUserDTO, String accessNumber, Long mainId, Integer size) {
            this.pushMarketingUserDTO = pushMarketingUserDTO;
            this.accessNumber = accessNumber;
            this.mainId = mainId;
            this.size = size;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = new Result<>();
            // 模拟推决策异常
            boolean b = toPolicyByRuleService.mockSwitch(pushMarketingUserDTO.getApiCode(),
                    MockSwitchEnum.GENERAL.getValue(), MockSwitchEnum.POLICYRETRY.getValue());
            if (b) {
                result.setCode(ResultCode.TIME_OUT.getValue());
            } else {
                result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                        accessNumber, size);
                if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())
                        || ResultCode.TIME_OUT.getValue().equals(result.getCode())) {
                    result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                            accessNumber, size);
                }
            }

            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                logger.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                        , "推送决策重试失败 accessNumber:" + accessNumber + " - " + JSON.toJSONString(result)));
            }

            if (ResultCode.TIME_OUT.getValue().equals(result.getCode()) ||
                    ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                insertErrorMark(pushMarketingUserDTO, mainId, accessNumber, size);
            }
            result.setDate(size);
            return result;
        }
    }

    // 插入错误标记
    private void insertErrorMark(PushMarketingUserDTO<PushMarketingUserTaskInfoDTO> pushMarketingUserDTO, Long mainId, String accessNumber, int size) {
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(pushMarketingUserDTO.getApiCode());
        errorMark.setmId(mainId);
        errorMark.setAccessNumber(accessNumber);
        errorMark.setPushSize(size);
        errorMark.setPolicyCondition(JSONObject.toJSONString(pushMarketingUserDTO));
        errorMark.setRetryStatus(RetryStatusEnum.AWAIT_COMPLETE.getValue());
        errorMark.setType(ErrorMarkTypeEnum.POLICY_ERROR.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }
}
