package com.br.marketing.rule.niwodai;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.NiwodaiRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


/**
 * 你我贷自动化转决策
 *
 * @author zhen.Li
 * @dateTime 2023/3/24 11:10
 */
@Service
@Slf4j
public class NiWoDaiPolicyTransferImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private PushRuleService pushRuleService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        JSONObject jsonObject = JSONObject.parseObject(transfer.getReserveField1());
        String status;
        if (("1".equals(jsonObject.getString("A"))) && ("0".equals(jsonObject.getString("B"))) && ("0".equals(jsonObject.getString("C")))) {
            status = "a";
        } else {
            status = "b";
        }
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());
        NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        String phone = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        pushMarketingUserDetailByRuleDTO.setPhone(phone);
        pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        JSONObject varDto = new JSONObject();
        varDto.put("userType", transfer.getUserType());
        varDto.put("status", status);
        pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + status + "_" + marketingCommonConfig.getNiWoDaiPushPolicyTargetApiCode());
        pushMarketingUserDetailByRuleDTO.setVariables(varDto);
        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        //去重参数设置
        pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        pushMarketingUserDetailByRuleDTO.setStatus(status);
        return pushMarketingUserDetailByRuleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                boolean statusA = ("1".equals(jsonObject.getString("A"))) && ("0".equals(jsonObject.getString("B"))) && ("0".equals(jsonObject.getString("C")));
                boolean statusB = ("1".equals(jsonObject.getString("A"))) && ("1".equals(jsonObject.getString("D"))) && ("0".equals(jsonObject.getString("F")));
                if (statusA || statusB) {
                    NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                            (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
                    // 检查有效期配置，非空时满足有效期
                    if (data.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "niwodai_TransferData_PolicyTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.NIWODAI_DATA_COLLECTION.getCode();
    }
}
