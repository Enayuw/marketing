package com.br.marketing.service.rulecenter.impl.push;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.SyncOperateTypeDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.*;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.rule.ai.policy.AbstractBaseAiToPolicy;
import com.br.marketing.rule.ai.policy.AiToPolicyProcessorFactory;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UploadRePushPolicyStrategy extends AbstractRuleCenterPushStrategy {

    @Autowired
    private MarketingSyncReportMapper syncReportMapper;

    @Autowired
    private MarketingJsonNodeParseMapper marketingJsonNodeParseMapper;


    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private DataCleanService dataCleanService;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Autowired
    protected AiToPolicyProcessorFactory strategyFactory;

    @Autowired
    RedisChgService redisChgService;


    private static final String TITLE = "[上传重推决策]";


    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        long start = System.currentTimeMillis();
        // 补推逻辑
        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(customerInfoPushMain.getmStatus())) {
            // 推决策重试
            toPolicyByRuleService.makeUpPolicyData(customerInfoPushMain,
                    MockSwitchEnum.GENERAL.getValue());

            Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                    FilterTypeEnum.UPLOAD_RE_POLICY.getValue());
            CustomerInfoPushMain main = new CustomerInfoPushMain();
            main.setId(customerInfoPushMain.getId());
            main.setmStatus(status);
            customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        Result<Boolean> result = new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
        //数据清洗
        ThreadPoolExecutor cleanPool = context.getEsThreadPool();
        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        String reportIds = pushMain.getUploadReportIds();
        String apiCode = pushMain.getmApiCode();
        String condition = getUploadDataCondition(pushMain.getmRuleCondition(), pushMain.getmApiCode());
        List<Long> listIds = Arrays.stream(reportIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        MarketingSyncReportExample syncReportExample = new MarketingSyncReportExample();
        syncReportExample.createCriteria().andIdIn(listIds);
        List<MarketingSyncReport> marketingSyncReports = syncReportMapper.selectByExample(syncReportExample);
        List<Future<Boolean>> resList = new ArrayList<>();
        //通用调用,查询清洗规则配置
        Map<String, MarketingDataCleanGeneralRuleConfig> configRule = dataCleanService.getConfigRule(apiCode,
                DataProcessEnum.DataTypeEnum.UPLOAD.getCode(), DataProcessEnum.AcceptTypeEnum.GENERAL.getCode(), DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
        if (CollectionUtils.isEmpty(configRule)) {
            log.warn(TITLE + "清洗配置为空，apiCode={}", apiCode);
            return result;
        }
        marketingSyncReports.forEach(syncreport -> {
            String appletDate = syncreport.getAppletDate();
            String userType = syncreport.getUserType();
            Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
            Long minId = null;
            while (true) {
                List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, null, appletDate, userType, createTime, condition, minId);
                if (CollectionUtils.isEmpty(syncUsers)) {
                    break;
                }
                minId = syncUsers.get(syncUsers.size() - 1).getId();
                List<List<MarketingSyncUser>> partitions = ListUtils.partition(syncUsers, 500);
                partitions.forEach(syncUserList -> {
                    resList.add(cleanPool.submit(() ->
                            cleanUploadData(syncUserList, configRule.values(), apiCode)));
                });
            }
        });
        //处理结果
        try {
            for (Future<Boolean> cleanFuture : resList) {
                Boolean pushRes = cleanFuture.get();
                if (!pushRes) {
                    result.setCode(ResultCode.FAIL.getValue());
                }
            }
        } catch (Exception ex) {
            log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
        }
        // 关闭线程池
        cleanPool.shutdown();
        try {
            while (!cleanPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商推送规则二线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        if (ResultCode.FAIL.getValue().equals(result.getCode())) {
            CustomerInfoPushMain main = new CustomerInfoPushMain();
            main.setId(customerInfoPushMain.getId());
            main.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
            customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        }
        log.warn(TITLE + "数据清洗结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
        return result;

    }

    protected Result<Boolean> validateData(RuleCenterPushContext context) {
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }


    private Boolean cleanUploadData(List<MarketingSyncUser> syncUserList, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList, String apiCode) {
        Boolean result = Boolean.TRUE;
        try {
            // 构建批量更新的字段值映射列表
            List<Map<String, Object>> batchFieldValueMaps = new ArrayList<>();
            List<Long> updateIds = new ArrayList<>();
            for (MarketingSyncUser syncUser : syncUserList) {
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(syncUser);
                jsonObject.put("requestId", syncUser.getRequestBatch());
                jsonObject.put("taskId", syncUser.getCusBatch());
                //清洗
                dataCleanService.uploadDetailCleanHandler(jsonObject, ruleConfigList, syncUser);
                Map<String, Object> fieldValueMap = buildFieldValueMap(syncUser, ruleConfigList);
                if (CollectionUtils.isEmpty(fieldValueMap)) {
                    continue;
                }
                batchFieldValueMaps.add(fieldValueMap);
                updateIds.add(syncUser.getId());

            }
            // 如果有数据需要更新，构建批量更新SQL
            if (!batchFieldValueMaps.isEmpty()) {
                String sql = buildBatchUpdateSql(apiCode, updateIds, batchFieldValueMaps);
                for (int i = 0; i < 3; i++) {
                    try {
                        marketingSyncInfoMapper.updateRepeatUserStatus(sql);
                        return Boolean.TRUE;
                    } catch (Exception e) {
                        log.error(TITLE + "清洗sql执行异常,apiCode={},sql={}", apiCode, sql);
                        result = Boolean.FALSE;
                    }
                }
            }
        } catch (Exception ex) {
            log.error(TITLE + "数据清洗线程执行异常" + ex.getMessage(), ex);
            result = Boolean.FALSE;
        }
        return result;
    }

    /**
     * 构建批量更新SQL - 使用CASE WHEN
     */
    private String buildBatchUpdateSql(String apiCode, List<Long> updateIds,
                                       List<Map<String, Object>> batchFieldValueMaps) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE b_marketing_sync_").append(apiCode).append(" SET ");
        // 为每个字段构建 CASE WHEN 语句
        List<String> setClauses = new ArrayList<>();
        // 从 batchFieldValueMaps 中获取字段名
        Set<String> fieldNames = batchFieldValueMaps.get(0).keySet();
        for (String fieldName : fieldNames) {
            StringBuilder caseWhen = new StringBuilder();
            caseWhen.append(fieldName).append(" = CASE id ");
            // 为每条记录构建 WHEN 条件
            for (int i = 0; i < updateIds.size(); i++) {
                Long id = updateIds.get(i);
                Map<String, Object> fieldValueMap = batchFieldValueMaps.get(i);
                Object fieldValue = fieldValueMap.get(fieldName);
                if (fieldValue != null) {
                    caseWhen.append("WHEN ").append(id).append(" THEN ");
                    caseWhen.append("'").append(fieldValue).append("' ");
                }
            }
            caseWhen.append("ELSE ").append(fieldName).append(" END");
            setClauses.add(caseWhen.toString());
        }

        sql.append(String.join(", ", setClauses));
        // 添加 WHERE 子句
        sql.append(" WHERE id IN (");
        sql.append(updateIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        sql.append(")");
        return sql.toString();
    }

    /**
     * 构建字段值映射
     */
    private Map<String, Object> buildFieldValueMap(MarketingSyncUser syncUser, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigs) {
        Map<String, Object> fieldValueMap = new HashMap<>();
        //剔除基础字段，并且无规则映射的字段
        ruleConfigs.removeIf(ruleConfig -> (!"dataItems.item.reserveField1".equals(ruleConfig.getParentPath()))
                && StringUtils.isEmpty(ruleConfig.getMappingRule()));
        ruleConfigs.removeIf(ruleConfig -> ("userType".equals(ruleConfig.getMappingField())
                && StringUtils.isEmpty(ruleConfig.getMappingRule())));
        List<String> mappingFields = ruleConfigs.stream().map(ruleConfig -> ruleConfig.getMappingField()).collect(Collectors.toList());
        Map<String, String> fieldMapping = getFieldNameMapping();
        for (String fieldName : mappingFields) {
            // 根据字段名获取对应的值（硬编码方式，性能更好）
            switch (fieldName) {
                case "cell":
                    fieldValueMap.put(fieldMapping.getOrDefault(fieldName, fieldName), syncUser.getCell());
                    break;
                case "name":
                    fieldValueMap.put(fieldMapping.getOrDefault(fieldName, fieldName), syncUser.getName());
                    break;
                case "id":
                    fieldValueMap.put(fieldMapping.getOrDefault(fieldName, fieldName), syncUser.getIdCard());
                    break;
                case "custNum":
                    fieldValueMap.put(fieldMapping.getOrDefault(fieldName, fieldName), syncUser.getCustNum());
                    break;
                case "operateType":
                    fieldValueMap.put(fieldMapping.getOrDefault(fieldName, fieldName), syncUser.getOperateType());
                    break;
                case "userType":
                    fieldValueMap.put(fieldMapping.getOrDefault(fieldName, fieldName), syncUser.getUserType());
                    break;
                default:
                    fieldValueMap.put("reserve_field1", syncUser.getReserveField1());
                    break;
            }
        }
        return fieldValueMap;
    }


    protected Result<Boolean> doExecutePush(RuleCenterPushContext context) {
        long start = System.currentTimeMillis();
        // 推送决策
        Boolean result = Boolean.TRUE;
        ThreadPoolExecutor pushPool = context.getPushThreadPool();
        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        String reportIds = pushMain.getUploadReportIds();
        String apiCode = pushMain.getmApiCode();
        String condition = getUploadDataCondition(pushMain.getmRuleCondition(), pushMain.getmApiCode());
        List<Long> listIds = Arrays.stream(reportIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        MarketingSyncReportExample syncReportExample = new MarketingSyncReportExample();
        syncReportExample.createCriteria().andIdIn(listIds);
        List<MarketingSyncReport> marketingSyncReports = syncReportMapper.selectByExample(syncReportExample);
        List<SyncOperateTypeDTO> operateTypeDTOList = syncReportMapper.selectOperateTypeGroup(apiCode, marketingSyncReports);
        //Reidskey自增,查询重推次数
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RedisKeyConstant.UPLOAD_REPUSH_POLICY_NUM.concat(date).concat(":").concat(apiCode);
        Long rePushCount = redisChgService.incr(key);
        redisChgService.expire(key, DateHelper.getRemainSecondsOneDay(new Date()));
        List<Future<Boolean>> resList = new ArrayList<>();
        //处理重复的数据
        Map<String, AtomicInteger> custNumMap = new HashMap<>();
        Map<String, AtomicInteger> cellMap = new HashMap<>();
        Set<String> batchNameSet = new HashSet<>();
        operateTypeDTOList.forEach(operateTypeDTO -> {
            String operateType = operateTypeDTO.getOperateType();
            if (StringUtils.isEmpty(operateType)) {
                log.error(TITLE + "存在operateType为空的数据");
                return;
            }
            // 根据不同的operateType添加额外条件
            switch (operateType) {
                case "3":
                    handleOperateTypeThree(pushPool, operateTypeDTO, condition, pushMain, rePushCount, resList, batchNameSet);
                    break;
                case "4":
                    handleOperateTypeFour(pushPool, operateTypeDTO, condition, pushMain, rePushCount, resList, batchNameSet);
                    break;
                case "5":
                    handleOperateTypeFive(pushPool, operateTypeDTO, condition, pushMain, rePushCount, custNumMap, resList, batchNameSet);
                    break;
                case "6":
                    handleOperateTypeSix(pushPool, operateTypeDTO, condition, pushMain, rePushCount, cellMap, resList, batchNameSet);
                    break;

                default:
                    log.error("未知的 operateType: {}", operateType);
                    break;
            }
        });
        //处理结果
        try {
            for (Future<Boolean> pushFuture : resList) {
                Boolean pushRes = pushFuture.get();
                if (!pushRes) {
                    result = Boolean.FALSE;
                }
            }
        } catch (Exception ex) {
            log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
        }
        // 关闭线程池
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商推送规则二线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        JSONObject extendJson = new JSONObject();
        extendJson.put("batchName", Joiner.on(",").join(batchNameSet));
        context.setExtendDataJson(extendJson);
        log.warn(TITLE + "推送决策结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(result);
    }


    protected void postProcess(RuleCenterPushContext context, Result<Boolean> result) {
        //处理状态
        Integer status;
        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        Boolean pushResult = result.getData();
        if (!pushResult) {
            status = PushRuleStatusEnum.PUSH_FAIL.getValue();
        } else {
            status = toPolicyByRuleService.queryExistError(pushMain.getId(),
                    FilterTypeEnum.UPLOAD_RE_POLICY.getValue());
        }
        String reportIds = pushMain.getUploadReportIds();
        //更新batchName
        String batchName = context.getExtendDataJson().getString("batchName");
        CustomerInfoPushMain update = new CustomerInfoPushMain();
        update.setmStatus(status);
        update.setBatchName(batchName);
        // 更新数据库状态
        update.setId(pushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(update);
    }

    private void handleOperateTypeFive(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain
            pushMain, Long rePushCount, Map<String, AtomicInteger> custNumMap, List<Future<Boolean>> resList, Set<String> batchNameSet) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            // 遍历syncUsers，将custNum+userType作为key，AtomicInteger自增作为value
            for (MarketingSyncUser syncUser : syncUsers) {
                String key = syncUser.getCustNum() + "_" + syncUser.getUserType();
                custNumMap.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
                syncUser.setReserveField1(setExtendField(syncUser.getReserveField1(), "rePeatNum", custNumMap.get(key)));
                syncUser.setReserveField1(setExtendField(syncUser.getReserveField1(), "rePushNum", rePushCount));
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                context.setCustomerTagsVO(tags);
                AbstractBaseAiToPolicy abstractBaseAiToPolicy = (AbstractBaseAiToPolicy) strategyFactory.getStrategy(operateType + "_RE");
                pushList.add(abstractBaseAiToPolicy.assembleData(syncUser, context));
            }
            //组装batchName
            Set<String> batchNames = pushList.stream().map(ruleDTO -> ruleDTO.getBatchName()).collect(Collectors.toSet());
            batchNameSet.addAll(batchNames);
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            resList.add(pushPool.submit(() -> uploadPushPolicy(pushList, pushMain)));
        }
    }


    private String setExtendField(String reserveField1, String field, Object result) {
        JSONObject jsonObject;
        if (com.br.marketing.common.utils.StringUtils.isNotEmpty(reserveField1)) {
            jsonObject = JSONObject.parseObject(reserveField1);
        } else {
            jsonObject = new JSONObject();
        }
        jsonObject.put(field, result);
        return jsonObject.toString();
    }

    private void handleOperateTypeSix(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain
            pushMain, Long rePushCount, Map<String, AtomicInteger> cellMap, List<Future<Boolean>> resList, Set<String> batchNameSet) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            // 遍历syncUsers，将cell+userType作为key，AtomicInteger自增作为value
            for (MarketingSyncUser syncUser : syncUsers) {
                String key = syncUser.getCell() + "_" + syncUser.getUserType();
                cellMap.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
                syncUser.setReserveField1(setExtendField(syncUser.getReserveField1(), "rePeatNum", cellMap.get(key)));
                syncUser.setReserveField1(setExtendField(syncUser.getReserveField1(), "rePushNum", rePushCount));
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                context.setCustomerTagsVO(tags);
                AbstractBaseAiToPolicy abstractBaseAiToPolicy = (AbstractBaseAiToPolicy) strategyFactory.getStrategy(operateType + "_RE");
                pushList.add(abstractBaseAiToPolicy.assembleData(syncUser, context));
            }
            //组装batchName
            Set<String> batchNames = pushList.stream().map(ruleDTO -> ruleDTO.getBatchName()).collect(Collectors.toSet());
            batchNameSet.addAll(batchNames);
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            resList.add(pushPool.submit(() -> uploadPushPolicy(pushList, pushMain)));

        }
    }

    private void handleOperateTypeFour(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain
            pushMain, Long rePushCount, List<Future<Boolean>> resList, Set<String> batchNameSet) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            for (MarketingSyncUser syncUser : syncUsers) {
                syncUser.setReserveField1(setExtendField(syncUser.getReserveField1(), "rePushNum", rePushCount));
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                context.setCustomerTagsVO(tags);
                AbstractBaseAiToPolicy abstractBaseAiToPolicy = (AbstractBaseAiToPolicy) strategyFactory.getStrategy(operateType + "_RE");
                pushList.add(abstractBaseAiToPolicy.assembleData(syncUser, context));
            }
            //组装batchName
            Set<String> batchNames = pushList.stream().map(ruleDTO -> ruleDTO.getBatchName()).collect(Collectors.toSet());
            batchNameSet.addAll(batchNames);
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            resList.add(pushPool.submit(() -> uploadPushPolicy(pushList, pushMain)));
        }
    }

    private void handleOperateTypeThree(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain
            pushMain, Long rePushCount, List<Future<Boolean>> resList, Set<String> batchNameSet) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            for (MarketingSyncUser syncUser : syncUsers) {
                syncUser.setReserveField1(setExtendField(syncUser.getReserveField1(), "rePushNum", rePushCount));
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                context.setCustomerTagsVO(tags);
                AbstractBaseAiToPolicy abstractBaseAiToPolicy = (AbstractBaseAiToPolicy) strategyFactory.getStrategy(operateType + "_RE");
                pushList.add(abstractBaseAiToPolicy.assembleData(syncUser, context));
            }
            //组装batchName
            Set<String> batchNames = pushList.stream().map(ruleDTO -> ruleDTO.getBatchName()).collect(Collectors.toSet());
            batchNameSet.addAll(batchNames);
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            resList.add(pushPool.submit(() ->
                    uploadPushPolicy(pushList, pushMain)));

        }
    }

    private Boolean uploadPushPolicy(List<PushMarketingUserDetailByRuleDTO> pushList, CustomerInfoPushMain pushMain) {
        Boolean pushResult = Boolean.TRUE;
        String apiCode = pushMain.getmApiCode();
        Map<String, List<PushMarketingUserDetailByRuleDTO>> batchMap = pushList.stream().collect(Collectors.groupingBy(PushMarketingUserDetailByRuleDTO::getBatchNumber));
        for (String batch : batchMap.keySet()) {
            List<PushMarketingUserDetailByRuleDTO> ruleLists = batchMap.get(batch);
            Map<String, List<PushMarketingUserDetailByRuleDTO>> strategyMap = ruleLists.stream().collect(Collectors.groupingBy(PushMarketingUserDetailByRuleDTO::getStrategyCode));
            for (String strategy : strategyMap.keySet()) {
                List<PushMarketingUserDetailByRuleDTO> datas = strategyMap.get(strategy);
                Map<String, List<PushMarketingUserDetailByRuleDTO>> batchNameMap = datas.stream()
                        .collect(Collectors.groupingBy(dto -> {
                            String batchName = dto.getBatchName();
                            return batchName != null ? batchName : ""; // 判空并返回默认值
                        }));
                for (String batchName : batchNameMap.keySet()) {
                    ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
                    Result<Integer> result = new Result<>();
                    List<PushMarketingUserDetailByRuleDTO> value = batchNameMap.get(batchName);
                    value.forEach(t -> {
                        PushMarketingUserDetailDTO entity = new PushMarketingUserDetailDTO();
                        BeanUtils.copyProperties(t, entity);
                        pushs.add(entity);
                    });
                    String accessNumber = UUID.randomUUID().toString();
                    Long mainId = pushMain.getId();
                    Integer size = pushs.size();
                    PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
                    taskInfoDTO.setData(pushs);
                    taskInfoDTO.setAccessNumber(accessNumber);
                    taskInfoDTO.setMethod("caseAdd");
                    taskInfoDTO.setBatchNumber(batch);
                    taskInfoDTO.setStrategyCode(strategy);
                    taskInfoDTO.setTaskId(pushMain.getId().toString());
                    if (!batchName.isEmpty()) {
                        taskInfoDTO.setBatchName(batchName);
                    }
                    PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                    pushMarketingUserDTO.setApiCode(apiCode);
                    pushMarketingUserDTO.setJsonData(taskInfoDTO);
                    result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                            accessNumber, size);
                    //重试
                    if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())
                            || ResultCode.TIME_OUT.getValue().equals(result.getCode())) {
                        result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                                accessNumber, size);
                    }
                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                                , "推送决策重试失败 accessNumber:" + accessNumber + " - " + JSON.toJSONString(result)));
                        if (ResultCode.TIME_OUT.getValue().equals(result.getCode()) ||
                                ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                            insertErrorMark(pushMarketingUserDTO, mainId, accessNumber, size);
                        } else {
                            pushResult = Boolean.FALSE;
                        }
                    }
                    result.setDate(size);
                }
            }
        }

        return pushResult;
    }

    // 插入错误标记
    private void insertErrorMark(PushMarketingUserDTO pushMarketingUserDTO, Long mainId, String accessNumber, int size) {
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(pushMarketingUserDTO.getApiCode());
        errorMark.setmId(mainId);
        errorMark.setAccessNumber(accessNumber);
        errorMark.setPushSize(size);
        errorMark.setPolicyCondition(JSONObject.toJSONString(pushMarketingUserDTO));
        errorMark.setRetryStatus(RetryStatusEnum.AWAIT_COMPLETE.getValue());
        errorMark.setType(ErrorMarkTypeEnum.POLICY_ERROR.getValue());
        errorMark.setFilterType(FilterTypeEnum.UPLOAD_RE_POLICY.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }


    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return null;
    }


    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return 0;
    }

    public String getUploadDataCondition(String ruleCondition, String apiCode) {
        //解析ruleCondition
        if (StringUtils.isEmpty(ruleCondition)) {
            return null;
        }
        JSONObject ruleConditionObject = JSON.parseObject(ruleCondition);
        JSONArray dataArray = ruleConditionObject.getJSONArray("data");
        if (CollectionUtils.isEmpty(dataArray)) {
            return null;
        }
        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(ruleConditionObject, "");
        MarketingJsonNodeParseExample jsonNodeParseExample = new MarketingJsonNodeParseExample();
        jsonNodeParseExample.createCriteria().andApiCodeEqualTo(apiCode).andDataTypeEqualTo(DataProcessEnum.UPLOAD_DATA_GENERAL.getDataType()).
                andAcceptTypeEqualTo(DataProcessEnum.UPLOAD_DATA_GENERAL.getAcceptType());
        List<MarketingJsonNodeParse> jsonNodeParseList = marketingJsonNodeParseMapper.selectByExample(jsonNodeParseExample);
        // 获取字段名映射关系
        Map<String, String> fieldMapping = getFieldNameMapping();

        //ParentPath为dataItems.item.reserveField1的节点
        List<MarketingJsonNodeParse> reserveField1Nodes = jsonNodeParseList.stream()
                .filter(node -> "dataItems.item.reserveField1".equals(node.getParentPath()))
                .collect(Collectors.toList());
        List<String> reserveField1Names = reserveField1Nodes.stream().map(MarketingJsonNodeParse::getNodeName)
                .collect(Collectors.toList());
        // 遍历reserveField1中的字段名，将sqlCondition中匹配的字段替换为JSON提取表达式
        for (String nodeName : reserveField1Names) {
            if (!sqlCondition.contains(nodeName)) {
                continue;
            }
            // 使用正则表达式匹配 - 使用边界匹配确保完整单词
            String pattern = "\\b" + Pattern.quote(nodeName) + "\\b";
            // 将字段名替换为 JSON_UNQUOTE(JSON_EXTRACT(reserve_field1, '$.字段名'))
            String jsonExtractExpr = String.format("JSON_UNQUOTE(JSON_EXTRACT(reserve_field1, '$.%s'))", nodeName);
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(sqlCondition);
            sqlCondition = m.replaceAll(Matcher.quoteReplacement(jsonExtractExpr));
        }
        // 处理其他ParentPath的节点，使用字段映射
        List<MarketingJsonNodeParse> otherNodes = jsonNodeParseList.stream()
                .filter(node -> !"dataItems.item.reserveField1".equals(node.getParentPath()))
                .collect(Collectors.toList());
        for (MarketingJsonNodeParse node : otherNodes) {
            String nodeName = node.getNodeName();
            if (!sqlCondition.contains(nodeName)) {
                continue;
            }
            // 检查是否有映射关系，如果有则使用映射后的字段名
            String mappedFieldName = fieldMapping.getOrDefault(nodeName, nodeName);
            // 使用正则表达式匹配原字段名
            String pattern = "\\b" + Pattern.quote(nodeName) + "\\b";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(sqlCondition);
            sqlCondition = m.replaceAll(Matcher.quoteReplacement(mappedFieldName));
        }
        return sqlCondition;
    }


    private void customizFieldMapping(String apiCode, JSONObject jsonObject) {
        HashMap<String, JSONObject> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
        JSONObject mapping = fieldKeyMapping.get(apiCode);
        if (ObjectUtil.isNotEmpty(mapping)) {
            for (String s : mapping.keySet()) {
                String toKey = mapping.getString(s);
                String oldV = jsonObject.getString(toKey);
                String newV = jsonObject.getString(s);
                if (StringUtils.isBlank(oldV) && StringUtils.isNotBlank(newV)) {
                    jsonObject.put(toKey, newV);
                }
            }
        }
    }

    /**
     * 构建营销同步用户的JSON对象
     *
     * @param jsonObject 目标JSON对象
     * @param syncUser   营销同步用户数据
     * @param jc3keyType 加密类型(null表示未配置)
     * @return 构建好的JSON对象
     */
    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {
        // 添加基础字段
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
        // 处理敏感信息(姓名和身份证)
        pushRuleService.processSensitiveInfo(jsonObject, syncUser, jc3keyType);
        // 添加用户姓名
        cusNameOfJo(syncUser.getName(), jsonObject);

        return jsonObject;
    }

    private String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
    }

    private void cusNameOfJo(String name, JSONObject jo) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        if (ObjectUtil.isEmpty(jo)) {
            return;
        }
        String cusName = jo.getString("cusName");
        if (StringUtils.isBlank(cusName)) {
            jo.put("cusName", BrCipherMaker.getInstance().decode(name));
        }
    }

    @Override
    protected RuleCenterPushContext setThreadPoolNum(RuleCenterPushContext pushContext) {
        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;

        Integer getJcNum = marketingCommonConfig.getScoreToJcThreadNum() != null
                && marketingCommonConfig.getScoreToJcThreadNum() > 0
                ? marketingCommonConfig.getScoreToJcThreadNum()
                : 2;
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);
        pushContext.setEsThreadPool(actionEs);
        pushContext.setPushThreadPool(pushJc);
        return pushContext;
    }

    protected RuleCenterPushContext assemblePushContext(CustomerInfoPushMain customerInfoPushMain) {
        RuleCenterPushContext pushContext = new RuleCenterPushContext();
        pushContext.setCustomerInfoPushMain(customerInfoPushMain);
        return pushContext;
    }

    /**
     * 获取上传字段名映射关系
     */
    private Map<String, String> getFieldNameMapping() {
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("id", "id_card");
        fieldMapping.put("custNum", "cust_num");
        fieldMapping.put("operateType", "operate_type");
        fieldMapping.put("userType", "user_type");
        fieldMapping.put("appletDate", "applet_date");
        fieldMapping.put("createTime", "create_time");
        return fieldMapping;
    }

}
