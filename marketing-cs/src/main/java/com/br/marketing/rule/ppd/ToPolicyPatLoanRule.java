package com.br.marketing.rule.ppd;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


@Service
public class ToPolicyPatLoanRule implements AssembleData<PushMarketingUserDetailByRuleDTO> {

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
        pushData.setPhone(syncUser.getCellMd5());
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());

        if (StringUtils.isNotBlank(reserveField1) && ObjectUtil.isNotEmpty(jsonObject)) {
            String batchNumber = ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? jsonObject.getString("batchNumber")
                    : (appletDate + "_" + apiCode);
            String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                    ? jsonObject.getString("strategyCode")
                    : "";
            String strategyCode = strategyCodeOriginal.length() < 12
                    ? strategyCodeOriginal
                    : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
            jsonObject.put("strategyCode", strategyCode);
            String userType  = strategyCodeOriginal.length() <= 12
                    ? emptyDefault(syncUser.getUserType())
                    : strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
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
            if(StringUtils.isNotEmpty(userType)){
                jsonObject.put("userType", userType);
            }
        }
        if (ObjectUtil.isEmpty(jsonObject)) {
            jsonObject = new JSONObject();
        }
        buildJson(jsonObject, syncUser);
        pushData.setVariables(jsonObject);
        return pushData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String operateType = syncUser.getOperateType();
            if (StringUtils.isNotBlank(operateType) && "3".equals(operateType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "PPDAI_To_Policy_PatLoan";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser) {
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("idCard", emptyDefault(syncUser.getIdCard()));
        jsonObject.put("name", emptyDefault(syncUser.getName()));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
//        jsonObject.put("userType", emptyDefault(syncUser.getUserType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
        return jsonObject;
    }

    private String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
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
