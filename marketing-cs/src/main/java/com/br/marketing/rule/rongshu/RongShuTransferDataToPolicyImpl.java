package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.MergeFieldService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * https://c.100credit.cn/pages/viewpage.action?pageId=178192841
 * 【紧急】D20240906榕树自动化转决策v3-4004643  情况2处理
 * 2024-10-28 apicode:4004643转化数据，按照规则生成后推送至4004733
 * <p>
 * 情况1 为调度任务  RongShuPushDecisionServiceImpl
 * <p>
 * 手机号去重时 情况1与情况2 使用同一把分布式锁
 *
 * @author Hua Qiang
 * @date 2024-09-06 21:18
 */
@Slf4j
@Service
public class RongShuTransferDataToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushRuleService pushRuleService;
    @Resource
    private MergeFieldService mergeFieldService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transferOld = (MarketingTransferSyncUser) transmitFact;
        MarketingTransferSyncUser transfer = new MarketingTransferSyncUser();
        BeanUtils.copyProperties(transferOld, transfer);
        // 情况2
        String status = null;
        String apiCode = context.getApiCode();
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(apiCode) != null) {
            encType = pushCellEncPolicy.get(apiCode);
        }
        HashMap<String, JSONObject> strategyCodeMap = marketingCommonConfig.getRongShuPushPolicyStrategyCode();
        JSONObject apiCodeReplace = strategyCodeMap.get("apiCodeReplace");
        // 2024-10-28 apicode:4004643转化数据，按照规则生成后推送至4004733
        if(null != apiCodeReplace && !apiCodeReplace.isEmpty()){
            if(StringUtils.isNotBlank(apiCodeReplace.getString(apiCode))){
                apiCode = apiCodeReplace.getString(apiCode);
            }else{
                log.warn("未发现rs-apiCode[{}]替换配置[{}]",apiCode, strategyCodeMap);
            }
        }
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());
        RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData = (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser marketingSyncUser = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        String cell = marketingSyncUser.getCell();
        pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell)));
        pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(cell));
        transfer.setApiCode(apiCode);
        JSONObject variables = new JSONObject();
        String reserveField1 = transfer.getReserveField1();
        if (JSON.isValidObject(reserveField1)) {
            JSONObject jsonObject = JSONObject.parseObject(reserveField1);
            String finalState = jsonObject.getString("finalState");
            if (StringUtils.isNotBlank(finalState)) {
                JSONObject strategyCodeObject = strategyCodeMap.get(apiCode);
                String strategyCode = strategyCodeObject.getString(finalState);
                if (null == strategyCode) {
                    String message = String.format("[%s]榕树自动化转决策出现非预期的finalState:[%s]", apiCode, finalState);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RONGSHU_PROCESS_WARNING.getCode()
                            , message , message));
                    return null;
                }else{
                    if("1".equals(finalState) || "2".equals(finalState)){
                        status = "2";
                    }else{
                        status = finalState;
                    }
                    pushMarketingUserDetailByRuleDTO.setStrategyCode(strategyCode);
                }
            }else{
                return null;
            }
        }else{
            return null;
        }
        variables.put("status", status);
        // 上传明细和转化明细合并
        mergeFieldService.mergeUploadAndTransfer(variables, transfer, marketingSyncUser
                , CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue());
        pushMarketingUserDetailByRuleDTO.setVariables(variables);
        pushMarketingUserDetailByRuleDTO.setBatchNumber(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + status + "_" + apiCode);
        //去重参数设置
        pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        pushMarketingUserDetailByRuleDTO.setStatus(status);
        pushMarketingUserDetailByRuleDTO.setPushApiCode(apiCode);
        return pushMarketingUserDetailByRuleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transferSyncUser.getReserveField1();
            if (JSON.isValidObject(reserveField1)) {
                JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                String finalState = jsonObject.getString("finalState");
                if (StringUtils.isNotEmpty(finalState)) {
                    RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                            (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
                    MarketingSyncUser marketingSyncUser = ruleNecessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
                    if (marketingSyncUser == null) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "RongShu_TransferData_To_Policy";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }
}
