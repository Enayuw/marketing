package com.br.marketing.rule.ai;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.AiToPolicyRecordExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;


@Service
@Slf4j
public class AiToPolicyPatLoanRuleOperaTypeFive implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CustomerTagsVO customerTagsVO = context.getCustomerTagsVO();
        PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        pushData.setInitId(syncUser.getId());
        pushData.setCaseNumber(syncUser.getCustNum());
        Integer jc3keyType = customerTagsVO.getPushJc3keyType();
        pushRuleService.judgeEncryptType(pushData, syncUser, jc3keyType);
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
        customizFieldMapping(context, jsonObject);

        if (StringUtils.isNotBlank(reserveField1) && ObjectUtil.isNotEmpty(jsonObject)) {
            String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                    ? jsonObject.getString("strategyCode")
                    : "";
            String strategyCode = strategyCodeOriginal.length() < 12
                    ? strategyCodeOriginal
                    : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
            jsonObject.put("strategyCode", strategyCode);
            String userType = strategyCodeOriginal.length() <= 12
                    ? emptyDefault(syncUser.getUserType())
                    : strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
            String batchNumber = syncUser.getReserveField2();
            String batchName = ObjectUtil.isNotEmpty(jsonObject.getString("batchName"))
                    ? jsonObject.getString("batchName")
                    : (appletDate + "_" + apiCode);
            String strategyName = ObjectUtil.isNotEmpty(jsonObject.getString("strategyName"))
                    ? jsonObject.getString("strategyName")
                    : "";
            if (StringUtils.isNotEmpty(strategyCode)) {
                pushData.setStrategyCode(strategyCode);
            } else {
                pushData.setStrategyCode("");
            }
            if (StringUtils.isNotEmpty(strategyCode)) {
                jsonObject.put("strategyName", strategyName);
            } else {
                jsonObject.put("strategyName", "");
            }
            pushData.setBatchNumber(batchNumber);
            pushData.setBatchName(batchName);
            jsonObject.put("batchName", batchName);
            if (StringUtils.isNotEmpty(userType)) {
                jsonObject.put("userType", userType);
            }
        }
        if (ObjectUtil.isEmpty(jsonObject)) {
            jsonObject = new JSONObject();
        }
        buildJson(jsonObject, syncUser, jc3keyType);
        pushData.setVariables(jsonObject);

        log.warn("AI自动化推决策_操作类型5,apiCode:{}", apiCode);
        return pushData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String operateType = syncUser.getOperateType();
            if (StringUtils.isNotBlank(operateType) && "5".equals(operateType)) {
                return insertRecord(syncUser);
            }
        }
        return false;
    }

    private boolean insertRecord(MarketingSyncUser syncUser) {
        String lockValue = UUID.randomUUID().toString();
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        Integer createDate = Integer.valueOf(yyyyMMdd);
        String apiCode = syncUser.getApiCode();
        String userType = syncUser.getUserType();
        String custNum = syncUser.getCustNum();
        String key = RedisKeyConstant.AI_TOPOLICY_PUSH_COUNTER.concat(String.format("%s:%s:%s:%s:%s", yyyyMMdd, apiCode, userType,
                CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode(), custNum));
        String batchNumber;

        try {
            redisChgService.lock(key, lockValue);
            try {
                AiToPolicyRecordExample example = new AiToPolicyRecordExample();
                example.createCriteria().andCreateDateEqualTo(createDate)
                        .andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                        .andRuleLabelEqualTo(CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode())
                        .andCustNumEqualTo(custNum);
                int pushCount = aiToPolicyRecordMapperBase.countByExample(example) + 1;
                batchNumber = yyyyMMdd + "-" + apiCode + "-5" + "-" + userType + "-" + pushCount;

                AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
                aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
                aiToPolicyRecord.setBatchNumber(batchNumber);
                aiToPolicyRecord.setApiCode(apiCode);
                aiToPolicyRecord.setUserType(userType);
                aiToPolicyRecord.setCustNum(custNum);
                aiToPolicyRecord.setRuleLabel(CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode());
                aiToPolicyRecord.setCreateDate(createDate);

                aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
                syncUser.setReserveField2(batchNumber);
                return true;
            } catch (DuplicateKeyException e) {
                log.warn("AI自动化推决策_操作类型5,数据重复，fingerprint:{}", syncUser.getFingerprint());
                return false;
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(), "AI自动化推决策_操作类型5,写去重表db异常："), e);
                return true;
            }
        } catch (Exception e) {
            redisChgService.unlock(key, lockValue);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
                    "AI自动化推决策_操作类型5,redis加锁异常,需要手动处理,apiCode：" + syncUser.getApiCode() + ",明细表id：" + syncUser.getId() + "。"), e);
            return false;
        } finally {
            redisChgService.unlock(key, lockValue);
        }
    }

    @Override
    public String label() {
        return CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode();
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    private void customizFieldMapping(ProcessHandlerContext context, JSONObject jsonObject) {
        HashMap<String, JSONObject> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
        JSONObject mapping = fieldKeyMapping.get(context.getApiCode());
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

}

