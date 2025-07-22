package com.br.marketing.rule.ai;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
@Slf4j
public class AiToPolicyPatLoanRuleOperaTypeFour implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushRuleService pushRuleService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CustomerTagsVO customerTagsVO = context.getCustomerTagsVO();
        PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        pushData.setInitId(syncUser.getId());
        pushData.setCaseNumber(syncUser.getCustNum());
        log.warn("进入自动化推决策规则AiToPolicyPatLoanRuleOperaTypeFour："+JSONObject.toJSONString(syncUser));
        String cellOriginal = syncUser.getCellOriginal();
        Integer jc3keyType = customerTagsVO.getPushJc3keyType();
        if (jc3keyType == null || jc3keyType.equals(CustomerTagsValue.PushJc3keyTypeEnum.PLAINTEXT.getValue())) {
            String decodedCell = BrCipherMaker.getInstance().decode(cellOriginal);
            // 未配置加密类型，判断是否log加密
            if (cellOriginal.equals(decodedCell)) {
                // 非log加密
                pushData.setPhone(cellOriginal);
            } else {
                // log加密
                pushData.setPhone(decodedCell);
            }
            pushData.setLogCell(syncUser.getCell());
        } else if (jc3keyType.equals(CustomerTagsValue.PushJc3keyTypeEnum.INIT.getValue())) {
            // 软交换
            pushData.setPhone(cellOriginal);
        } else {
            // 其他加密类型
            pushData.setPhone(cellOriginal);
            pushData.setLogCell(syncUser.getCell());
        }
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
        customizFieldMapping(context,jsonObject);

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
            String batchNumber = ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? jsonObject.getString("batchNumber")
                    : (appletDate + "_" + apiCode + "_" + userType);
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

        log.warn("AI自动化推决策_操作类型4,apiCode:{}", apiCode);
        return pushData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String operateType = syncUser.getOperateType();
            if (StringUtils.isNotBlank(operateType) && "4".equals(operateType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FOUR.getCode();
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
     *
     * @param jsonObject 目标JSON对象
     * @param syncUser 营销同步用户数据
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
        processSensitiveInfo(jsonObject, syncUser, jc3keyType);

        // 添加用户姓名
        cusNameOfJo(syncUser.getName(), jsonObject);

        return jsonObject;
    }

    /**
     * 处理敏感信息(姓名和身份证)的加密逻辑
     */
    private void processSensitiveInfo(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {
        String idCard = syncUser.getIdCardOriginal();
        String name = syncUser.getNameOriginal();

        if (jc3keyType == null) {
            // 未配置加密类型，尝试解密LOG加密
            BrCipherMaker cipher = BrCipherMaker.getInstance();
            String decodedIdCard = cipher.decode(idCard);
            String decodedName = cipher.decode(name);

            // 根据解密结果判断是否LOG加密
            jsonObject.put("idCard", idCard.equals(decodedIdCard) ? idCard : decodedIdCard);
            jsonObject.put("name", name.equals(decodedName) ? name : decodedName);
        } else {
            // 配置了加密类型，直接使用原始值
            jsonObject.put("idCard", idCard);
            jsonObject.put("name", name);
        }
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

