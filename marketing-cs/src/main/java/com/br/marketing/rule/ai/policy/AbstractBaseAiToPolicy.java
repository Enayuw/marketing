package com.br.marketing.rule.ai.policy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * AI推决策规则抽象基类
 * 定义模板方法，提供通用的业务逻辑
 * 子类可以选择性重写原子性方法
 */
@Slf4j
public abstract class AbstractBaseAiToPolicy implements AiToPolicyProcessor {
    @Autowired
    protected MarketingCommonConfig marketingCommonConfig;
    @Autowired
    protected PushRuleService pushRuleService;
    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    public PushMarketingUserDetailByRuleDTO assembleData(Object transmitFact, ProcessHandlerContext context) {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        CustomerTagsVO customerTagsVO = context.getCustomerTagsVO();

        // 创建PushData对象
        PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();

        // 设置基础字段
        setInitId(pushData, syncUser);
        setCaseNumber(pushData, syncUser);

        // 处理加密类型
        processEncryptType(pushData, syncUser, customerTagsVO);

        // 解析reserveField1
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());

        // 执行字段映射
        executeFieldMapping(context, jsonObject);

        // 处理策略相关字段
        processStrategyFields(pushData, syncUser, jsonObject);

        // 构建JSON对象
        buildJsonObject(jsonObject, syncUser, customerTagsVO.getPushJc3keyType());

        // 设置变量
        setVariables(pushData, jsonObject);

        // 定制化逻辑
        handleCustomizeLogic(pushData, syncUser, context, jsonObject);

        log.warn("AI自动化推决策_操作类型{},apiCode:{}", getOperationType(), syncUser.getApiCode());
        return pushData;
    }

    public boolean insertRecord(MarketingSyncUser syncUser) {
        AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
        aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
        aiToPolicyRecord.setUserType(syncUser.getUserType());
        aiToPolicyRecord.setCustNum(syncUser.getCustNum());
        aiToPolicyRecord.setApiCode(syncUser.getApiCode());
        aiToPolicyRecord.setRuleLabel(CommonRuleLabelEnum.AI_TO_POLICY.getCode());
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        aiToPolicyRecord.setCreateDate(Integer.valueOf(yyyyMMdd));

        try {
            aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
            return true;
        } catch (DuplicateKeyException e) {
            log.warn("AI自动化推决策,数据重复，fingerprint:{}", syncUser.getFingerprint());
            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
                    "AI自动化推决策,写去重表db异常："), e);
            return true;
        }
    }

    // ==================== 原子性方法 - 子类可选择重写 ====================
    /**
     * 设置初始化ID
     */
    protected void setInitId(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser) {
        pushData.setInitId(syncUser.getId());
    }

    /**
     * 设置案件编号
     */
    protected void setCaseNumber(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser) {
        pushData.setCaseNumber(syncUser.getCustNum());
    }

    /**
     * 处理加密类型
     */
    protected void processEncryptType(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser, CustomerTagsVO customerTagsVO) {
        Integer jc3keyType = customerTagsVO.getPushJc3keyType();
        pushRuleService.judgeEncryptType(pushData, syncUser, jc3keyType);
    }

    /**
     * 执行字段映射
     */
    protected void executeFieldMapping(ProcessHandlerContext context, JSONObject jsonObject) {
        // 默认实现：空方法，子类可重写
    }

    /**
     * 处理策略相关字段
     */
    protected void processStrategyFields(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser, JSONObject jsonObject) {
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");

        if (StringUtils.isNotBlank(syncUser.getReserveField1()) && ObjectUtil.isNotEmpty(jsonObject)) {
            // 获取原始策略代码
            String strategyCodeOriginal = getStrategyCodeOriginal(jsonObject);

            // 处理策略代码
            processStrategyCode(jsonObject, strategyCodeOriginal);

            // 处理用户类型
            processUserType(jsonObject, syncUser, strategyCodeOriginal);

            // 生成批次号
            String batchNumber = generateBatchNumber(syncUser);
            pushData.setBatchNumber(batchNumber);

            // 生成批次名称
            String batchName = generateBatchName(jsonObject, appletDate, apiCode);
            pushData.setBatchName(batchName);
            jsonObject.put("batchName", batchName);

            // 处理策略名称
            processStrategyName(jsonObject, pushData);

            // 设置用户类型到JSON
            setUserTypeToJson(jsonObject, syncUser);
        }
    }

    /**
     * 获取原始策略代码
     */
    protected String getStrategyCodeOriginal(JSONObject jsonObject) {
        String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                ? jsonObject.getString("strategyCode")
                : "";
        return strategyCodeOriginal;
    }

    /**
     * 处理策略代码
     */
    protected void processStrategyCode(JSONObject jsonObject, String strategyCodeOriginal) {
        String strategyCode = strategyCodeOriginal.length() < 12
                ? strategyCodeOriginal
                : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
        jsonObject.put("strategyCode", strategyCode);
    }

    /**
     * 处理用户类型
     */
    protected void processUserType(JSONObject jsonObject, MarketingSyncUser syncUser, String strategyCodeOriginal) {
        String userType = strategyCodeOriginal.length() <= 12
                ? emptyDefault(syncUser.getUserType())
                : strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
        jsonObject.put("userType", userType);
    }

    /**
     * 生成批次名称
     */
    protected String generateBatchName(JSONObject jsonObject, String appletDate, String apiCode) {
        return ObjectUtil.isNotEmpty(jsonObject.getString("batchName"))
                ? jsonObject.getString("batchName")
                : (appletDate + "_" + apiCode);
    }

    /**
     * 处理策略名称
     */
    protected void processStrategyName(JSONObject jsonObject, PushMarketingUserDetailByRuleDTO pushData) {
        String strategyCode = jsonObject.getString("strategyCode");
        String strategyName = ObjectUtil.isNotEmpty(jsonObject.getString("strategyName"))
                ? jsonObject.getString("strategyName")
                : "";

        if (StringUtils.isNotEmpty(strategyCode)) {
            pushData.setStrategyCode(strategyCode);
            jsonObject.put("strategyName", strategyName);
        } else {
            pushData.setStrategyCode("");
            jsonObject.put("strategyName", "");
        }
    }

    /**
     * 设置用户类型到JSON
     */
    protected void setUserTypeToJson(JSONObject jsonObject, MarketingSyncUser syncUser) {
        String userType = jsonObject.getString("userType");
        if (StringUtils.isNotEmpty(userType)) {
            jsonObject.put("userType", userType);
        }
    }

    /**
     * 构建JSON对象
     */
    protected void buildJsonObject(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {
        if (ObjectUtil.isEmpty(jsonObject)) {
            jsonObject = new JSONObject();
        }

        // 添加基础字段
        addBasicFields(jsonObject, syncUser);

        // 处理敏感信息
        processSensitiveInfo(jsonObject, syncUser, jc3keyType);

        // 添加用户姓名
        addUserName(jsonObject, syncUser);

        //剔除重推次数，重复次数字段
        jsonObject.remove("rePeatNum");
        jsonObject.remove("rePushNum");

    }

    /**
     * 添加基础字段
     */
    protected void addBasicFields(JSONObject jsonObject, MarketingSyncUser syncUser) {
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
    }

    /**
     * 处理敏感信息
     */
    protected void processSensitiveInfo(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {
        pushRuleService.processSensitiveInfo(jsonObject, syncUser, jc3keyType);
    }

    /**
     * 添加用户姓名
     */
    protected void addUserName(JSONObject jsonObject, MarketingSyncUser syncUser) {
        String name = syncUser.getName();
        if (StringUtils.isBlank(name)) {
            return;
        }
        if (ObjectUtil.isEmpty(jsonObject)) {
            return;
        }
        String cusName = jsonObject.getString("cusName");
        if (StringUtils.isBlank(cusName)) {
            jsonObject.put("cusName", BrCipherMaker.getInstance().decode(name));
        }
    }

    /**
     * 设置变量
     */
    protected void setVariables(PushMarketingUserDetailByRuleDTO pushData, JSONObject jsonObject) {
        pushData.setVariables(jsonObject);
    }

    /**
     * 定制化逻辑
     */
    protected void handleCustomizeLogic(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser, ProcessHandlerContext context,
                                         JSONObject jsonObject) {
        // 默认实现：空方法，子类可重写
    }

    /**
     * 空值默认处理
     */
    protected String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
    }

    // ==================== 抽象方法 - 子类必须实现 ====================

}