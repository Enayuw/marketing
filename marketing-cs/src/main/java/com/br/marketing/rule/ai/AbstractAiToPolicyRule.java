//package com.br.marketing.rule.ai;
//
//import cn.hutool.core.util.ObjectUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.br.common.log.AlertLog;
//import com.br.common.util.BrCipherMaker;
//import com.br.marketing.client.RedisChgService;
//import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
//import com.br.marketing.common.enums.AlarmSendCodeEnum;
//import com.br.marketing.common.utils.DateHelper;
//import com.br.marketing.context.ProcessHandlerContext;
//import com.br.marketing.entity.AiToPolicyRecord;
//import com.br.marketing.entity.AiToPolicyRecordExample;
//import com.br.marketing.entity.MarketingSyncUser;
//import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
//import com.br.marketing.rule.AssembleData;
//import com.br.marketing.rule.ai.strategy.AiToPolicyOperationStrategy;
//import com.br.marketing.rule.ai.strategy.AiToPolicyStrategyFactory;
//import com.br.marketing.service.PushRuleService;
//import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
//import com.br.marketing.speedconfig.MarketingCommonConfig;
//import com.br.marketing.strategy.InterfaceHandlerEnum;
//import com.google.common.collect.Lists;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;
//
///**
// * AI推决策抽象基类
// * 使用模板方法模式定义算法骨架，策略模式处理差异化逻辑
// */
//@Service
//@Slf4j
//public class AbstractAiToPolicyRule implements AssembleData<PushMarketingUserDetailByRuleDTO> {
//
//    @Autowired
//    protected MarketingCommonConfig marketingCommonConfig;
//
//    @Autowired
//    protected PushRuleService pushRuleService;
//
//    @Autowired
//    protected AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;
//
//    @Autowired
//    protected RedisChgService redisChgService;
//
//    @Autowired
//    private AiToPolicyStrategyFactory strategyFactory;
//
//    /**
//     * 模板方法：组装数据的主流程
//     */
//    @Override
//    public final PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
//        CustomerTagsVO customerTagsVO = context.getCustomerTagsVO();
//        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
//
//        // 获取对应的策略
//        AiToPolicyOperationStrategy strategy = strategyFactory.getStrategy(syncUser.getOperateType());
//
//        PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
//        pushData.setInitId(syncUser.getId());
//        pushData.setCaseNumber(syncUser.getCustNum());
//
//        Integer jc3keyType = customerTagsVO.getPushJc3keyType();
//        pushRuleService.judgeEncryptType(pushData, syncUser, jc3keyType);
//
//        String apiCode = syncUser.getApiCode();
//        String appletDate = syncUser.getAppletDate().replace("-", "");
//        String reserveField1 = syncUser.getReserveField1();
//        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
//
//        customizFieldMapping(context, jsonObject);
//        processStrategyData(syncUser, pushData, jsonObject, appletDate, apiCode, strategy);
//
//        if (ObjectUtil.isEmpty(jsonObject)) {
//            jsonObject = new JSONObject();
//        }
//
//        buildJson(jsonObject, syncUser, jc3keyType);
//        pushData.setVariables(jsonObject);
//
//        log.warn("AI自动化推决策_操作类型{},apiCode:{}", strategy.getOperationType(), apiCode);
//        return pushData;
//    }
//
//    /**
//     * 模板方法：判断是否需要组装数据
//     */
//    @Override
//    public final boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
//        if (transmitFact instanceof MarketingSyncUser) {
//            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
//            String operateType = syncUser.getOperateType();
//
//            // 检查是否支持该操作类型，并且是当前子类负责的类型
//            List<String> RESULT_CODE = Lists.newArrayList("4", "5", "6");
//            String[] strings = {"4", "5", "6"};
//            if (StringUtils.isNotBlank(operateType) &&
//                    (RESULT_CODE.contains(operateType))) {
//
//                return insertRecord(syncUser);
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 处理策略相关数据
//     */
//    private void processStrategyData(MarketingSyncUser syncUser, PushMarketingUserDetailByRuleDTO pushData,
//                                   JSONObject jsonObject, String appletDate, String apiCode,
//                                   AiToPolicyOperationStrategy strategy) {
//        String reserveField1 = syncUser.getReserveField1();
//
//        if (StringUtils.isNotBlank(reserveField1) && ObjectUtil.isNotEmpty(jsonObject)) {
//            String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
//                    ? jsonObject.getString("strategyCode")
//                    : "";
//            String strategyCode = strategyCodeOriginal.length() < 12
//                    ? strategyCodeOriginal
//                    : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
//            jsonObject.put("strategyCode", strategyCode);
//
//            String userType = strategyCodeOriginal.length() <= 12
//                    ? emptyDefault(syncUser.getUserType())
//                    : strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
//
//            // 使用策略获取批次号
//            String batchNumber = strategy.getBatchNumber(syncUser, appletDate, apiCode, userType, jsonObject);
//            String batchName = ObjectUtil.isNotEmpty(jsonObject.getString("batchName"))
//                    ? jsonObject.getString("batchName")
//                    : (appletDate + "_" + apiCode);
//            String strategyName = ObjectUtil.isNotEmpty(jsonObject.getString("strategyName"))
//                    ? jsonObject.getString("strategyName")
//                    : "";
//
//            if (StringUtils.isNotEmpty(strategyCode)) {
//                pushData.setStrategyCode(strategyCode);
//                jsonObject.put("strategyName", strategyName);
//            } else {
//                pushData.setStrategyCode("");
//                jsonObject.put("strategyName", "");
//            }
//
//            pushData.setBatchNumber(batchNumber);
//            pushData.setBatchName(batchName);
//            jsonObject.put("batchName", batchName);
//
//            if (StringUtils.isNotEmpty(userType)) {
//                jsonObject.put("userType", userType);
//            }
//        }
//    }
//
//    /**
//     * 插入记录的模板方法
//     */
//    private boolean insertRecord(MarketingSyncUser syncUser) {
//        AiToPolicyOperationStrategy strategy = strategyFactory.getStrategy(syncUser.getOperateType());
//
//        if (strategy.needsRedisLock()) {
//            return insertRecordWithLock(syncUser, strategy);
//        } else {
//            return insertRecordWithoutLock(syncUser, strategy);
//        }
//    }
//
//    /**
//     * 使用Redis锁插入记录
//     */
//    private boolean insertRecordWithLock(MarketingSyncUser syncUser, AiToPolicyOperationStrategy strategy) {
//        String lockValue = UUID.randomUUID().toString();
//        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
//        Integer createDate = Integer.valueOf(yyyyMMdd);
//        String apiCode = syncUser.getApiCode();
//        String userType = syncUser.getUserType();
//
//        String key = strategy.generateRedisKey(yyyyMMdd, apiCode, userType, syncUser, "new label");
//
//        try {
//            redisChgService.lock(key, lockValue);
//
//            // 处理特殊逻辑
//            strategy.handleSpecialLogic(syncUser);
//
//            try {
//                AiToPolicyRecordExample example = new AiToPolicyRecordExample();
//                example.createCriteria()
//                        .andCreateDateEqualTo(createDate)
//                        .andApiCodeEqualTo(apiCode)
//                        .andUserTypeEqualTo(userType)
//                        .andRuleLabelEqualTo("new label")
//                        .andCustNumEqualTo(strategy.getDeduplicationValue(syncUser));
//
//                int pushCount = aiToPolicyRecordMapperBase.countByExample(example) + 1;
//                String batchNumber = strategy.generateDynamicBatchNumber(yyyyMMdd, apiCode,
//                        strategy.getOperationType(), userType, pushCount);
//
//                AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
//                aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
//                aiToPolicyRecord.setBatchNumber(batchNumber);
//                aiToPolicyRecord.setApiCode(apiCode);
//                aiToPolicyRecord.setUserType(userType);
//                aiToPolicyRecord.setCustNum(strategy.getDeduplicationValue(syncUser));
//                aiToPolicyRecord.setRuleLabel("new label");
//                aiToPolicyRecord.setCreateDate(createDate);
//
//                aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
//                syncUser.setReserveField2(batchNumber);
//                return true;
//
//            } catch (DuplicateKeyException e) {
//                log.warn("AI自动化推决策_操作类型{},数据重复，fingerprint:{}", strategy.getOperationType(), syncUser.getFingerprint());
//                return false;
//            } catch (Exception e) {
//                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
//                        "AI自动化推决策_操作类型" + strategy.getOperationType() + ",写去重表db异常："), e);
//                return true;
//            }
//        } catch (Exception e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
//                    "AI自动化推决策_操作类型" + strategy.getOperationType() + ",redis加锁异常,需要手动处理,apiCode：" +
//                    syncUser.getApiCode() + ",明细表id：" + syncUser.getId() + "。"), e);
//            return false;
//        } finally {
//            redisChgService.unlock(key, lockValue);
//        }
//    }
//
//    /**
//     * 不使用Redis锁插入记录
//     */
//    private boolean insertRecordWithoutLock(MarketingSyncUser syncUser, AiToPolicyOperationStrategy strategy) {
//        try {
//            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
//
//            AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
//            aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
//            aiToPolicyRecord.setUserType(syncUser.getUserType());
//            aiToPolicyRecord.setCustNum(syncUser.getCustNum());
//            aiToPolicyRecord.setApiCode(syncUser.getApiCode());
//            aiToPolicyRecord.setRuleLabel("new label");
//            aiToPolicyRecord.setCreateDate(Integer.valueOf(yyyyMMdd));
//
//            aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
//            return true;
//        } catch (DuplicateKeyException e) {
//            log.warn("AI自动化推决策_操作类型{},数据重复，fingerprint:{}", strategy.getOperationType(), syncUser.getFingerprint());
//            return false;
//        } catch (Exception e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
//                    "AI自动化推决策_操作类型" + strategy.getOperationType() + ",写去重表db异常："), e);
//            return true;
//        }
//    }
//
//    /**
//     * 子类必须实现：返回支持的操作类型
//     */
//    protected String getSupportedOperationType() {
//        return null;
//    };
//
//    @Override
//    public Integer dataDirection() {
//        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
//    }
//
//    @Override
//    public Integer ruleDataCollection() {
//        return null;
//    }
//
//    private void customizFieldMapping(ProcessHandlerContext context, JSONObject jsonObject) {
//        HashMap<String, JSONObject> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
//        JSONObject mapping = fieldKeyMapping.get(context.getApiCode());
//        if (ObjectUtil.isNotEmpty(mapping)) {
//            for (String s : mapping.keySet()) {
//                String toKey = mapping.getString(s);
//                String oldV = jsonObject.getString(toKey);
//                String newV = jsonObject.getString(s);
//                if (StringUtils.isBlank(oldV) && StringUtils.isNotBlank(newV)) {
//                    jsonObject.put(toKey, newV);
//                }
//            }
//        }
//    }
//
//    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {
//        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
//        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
//        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
//        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
//        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
//        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
//        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
//        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
//
//        pushRuleService.processSensitiveInfo(jsonObject, syncUser, jc3keyType);
//        cusNameOfJo(syncUser.getName(), jsonObject);
//
//        return jsonObject;
//    }
//
//    private String emptyDefault(String value) {
//        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
//    }
//
//    private void cusNameOfJo(String name, JSONObject jo) {
//        if (StringUtils.isBlank(name)) {
//            return;
//        }
//        if (ObjectUtil.isEmpty(jo)) {
//            return;
//        }
//        String cusName = jo.getString("cusName");
//        if (StringUtils.isBlank(cusName)) {
//            jo.put("cusName", BrCipherMaker.getInstance().decode(name));
//        }
//    }
//
//    @Override
//    public String label() {
//        return "AI_To_Policy";
//    }
//}