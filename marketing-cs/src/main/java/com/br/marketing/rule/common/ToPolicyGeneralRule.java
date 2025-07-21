package com.br.marketing.rule.common;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@Slf4j
public class ToPolicyGeneralRule implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    MarketingCommonConfig marketingCommonConfig;


    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CustomerTagsVO customerTagsVO = context.getCustomerTagsVO();
        List<String> apiCodeOfpushPolicy = marketingCommonConfig.getApiCodeOfpushPolicy();
        PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        pushData.setInitId(syncUser.getId());
        pushData.setCaseNumber(syncUser.getCustNum());
        log.warn("进入自动化推决策规则ToPolicyGeneralRule："+JSONObject.toJSONString(syncUser));
        String cellOriginal = syncUser.getCellOriginal();
        Integer jc3keyType = customerTagsVO.getPushJc3keyType();
        if (jc3keyType == null) {
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

        if (StringUtils.isNotBlank(reserveField1) && ObjectUtil.isNotEmpty(jsonObject)) {
            String batchNumber = "";

            if (ObjectUtil.isNotEmpty(apiCodeOfpushPolicy) && apiCodeOfpushPolicy.contains(apiCode)) {
                batchNumber = ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                        ? (appletDate + jsonObject.getString("batchNumber"))
                        : (appletDate + "_" + apiCode);
            } else {
                batchNumber = ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                        ? jsonObject.getString("batchNumber")
                        : (appletDate + "_" + apiCode);
            }
            String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                    ? jsonObject.getString("strategyCode")
                    : "";
            String strategyCode = strategyCodeOriginal.length() < 12
                    ? strategyCodeOriginal
                    : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
            String userType = strategyCodeOriginal.length() <= 12
                    ? emptyDefault(syncUser.getUserType())
                    : strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
            userType = StringUtils.isEmpty(userType) ? "" : userType;
            jsonObject.put("userType", userType);
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
        }
        if (ObjectUtil.isEmpty(jsonObject)) {
            jsonObject = new JSONObject();
        }
        buildJson(jsonObject, syncUser);
        pushData.setVariables(jsonObject);
        log.warn("AI自动化推决策_操作类型3,apiCode:{}", apiCode);
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
        return CommonRuleLabelEnum.TO_POLICY_GENERAL.getCode();
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
        jsonObject.put("idCard", emptyDefault(syncUser.getIdCardOriginal()));
        jsonObject.put("name", emptyDefault(syncUser.getNameOriginal()));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
        cusNameOfJo(syncUser.getName(),jsonObject);
        return jsonObject;
    }

    private String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
    }

    private void cusNameOfJo(String name,JSONObject jo){
        if(StringUtils.isBlank(name)){
            return;
        }
        if(ObjectUtil.isEmpty(jo)){
            return;
        }
        String cusName = jo.getString("cusName");
        if(StringUtils.isBlank(cusName)){
            jo.put("cusName",BrCipherMaker.getInstance().decode(name));
        }
    }

}
