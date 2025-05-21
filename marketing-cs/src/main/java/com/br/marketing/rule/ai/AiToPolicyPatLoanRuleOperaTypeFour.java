package com.br.marketing.rule.ai;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
        pushData.setPhone(syncUser.getCellSha256());
        pushData.setPhone(get3keyValue(syncUser.getCell(), "cell", customerTagsVO.getPushJc3keyType()));
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
        buildJson(jsonObject, syncUser, customerTagsVO.getPushJc3keyType());
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
        return "AI_To_Policy_PatLoan_OperaType_Four";
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
        HashMap<String, HashMap<String, String>> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
        HashMap<String, String> map = fieldKeyMapping.get(context.getApiCode());
        if (ObjectUtil.isNotEmpty(map)) {
            for (String s : map.keySet()) {
                String toKey = map.get(s);
                String oldV = jsonObject.getString(toKey);
                String newV = jsonObject.getString(s);
                if (StringUtils.isBlank(oldV) && StringUtils.isNotBlank(newV)) {
                    jsonObject.put(toKey, newV);
                }
            }
        }
    }


    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser, Integer pushJc3keyType) {
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("idCard", emptyDefault(get3keyValue(syncUser.getIdCard(), "idCard", pushJc3keyType)));
        jsonObject.put("name", emptyDefault(get3keyValue(syncUser.getName(), "name", pushJc3keyType)));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
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

    private String get3keyValue(String content, String contentType, Integer encryptionType) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.INIT.getValue().equals(encryptionType)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.SHA256_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return StringUtils.isNotBlank(decode) ? Sha256Util.getSHA256Encrypt(decode) : content;
        }
        return null;
    }
}

