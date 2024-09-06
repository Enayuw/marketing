package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * https://c.100credit.cn/pages/viewpage.action?pageId=178192841
 * 【紧急】D20240906榕树自动化转决策v3-4004643  情况2处理
 *
 * @author Hua Qiang
 * @date 2024-09-06 21:18
 */
@Service
public class RongShuTransferDataToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushRuleService pushRuleService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String status = "2";
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());
        RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData = (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser marketingSyncUser = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        String cell = marketingSyncUser.getCell();
        pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell)));
        pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(cell));
        pushMarketingUserDetailByRuleDTO.setBatchNumber(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + status + "_" + context.getApiCode());
        pushMarketingUserDetailByRuleDTO.setVariables((JSONObject) JSON.toJSON(transfer));
        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        //去重参数设置
        pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        pushMarketingUserDetailByRuleDTO.setStatus(status);
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
