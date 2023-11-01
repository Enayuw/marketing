package com.br.marketing.rule.gome;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.GomeRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

/**
 * 国美自动化转决策
 *
 * @author zhen.Li
 * @dateTime 2023/4/03 11:10
 */
@Service
@Slf4j
public class GomePolicyTransferImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private PushRuleService pushRuleService;

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS, Locale.CHINA);

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String status;
        if (StringUtils.isNotEmpty(transfer.getLoginTime()) && "0".equals(transfer.getIfApply()) && "1".equals(transfer.getIfLogin())
                && LocalDate.now().minusDays(1).isEqual(LocalDateTime.parse(transfer.getLoginTime(), DATE_TIME_FORMATTER).toLocalDate())) {
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
        GomeRuleCollectDataImpl.GomeRuleNecessaryData data =
                (GomeRuleCollectDataImpl.GomeRuleNecessaryData) context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO bo = data.getSyncUserValidityPeriodMap().get(transfer.getCustNum());
        String cell = bo.getSyncUsers().get(0).getCell();
        pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell)));
        pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(cell));
        pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + context.getApiCode() + "_" + status);
        pushMarketingUserDetailByRuleDTO.setVariables(new JSONObject());
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
            String applyLoan = JSON.parseObject(transfer.getReserveField1()).getString("applyLoan");
            boolean statusA = StringUtils.isNotEmpty(transfer.getLoginTime()) && "0".equals(transfer.getIfApply()) && "1".equals(transfer.getIfLogin())
                    && LocalDate.now().minusDays(1).isEqual(LocalDateTime.parse(transfer.getLoginTime(), DATE_TIME_FORMATTER).toLocalDate());
            boolean statusB = StringUtils.isNotEmpty(transfer.getAuditTime()) && "0".equals(applyLoan) && "1".equals(transfer.getApplyResult()) &&
                    LocalDate.now().minusDays(1).isEqual(LocalDateTime.parse(transfer.getAuditTime(), DATE_TIME_FORMATTER).toLocalDate());
            if (statusA || statusB) {
                GomeRuleCollectDataImpl.GomeRuleNecessaryData data =
                        (GomeRuleCollectDataImpl.GomeRuleNecessaryData) context.getRuleNecessaryData();
                // 检查有效期配置，非空时满足有效期
                if (data.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) != null) {
                    return true;
                }
            }
        }
        return false;

    }


    @Override
    public String label() {
        return "Gome_TransferData_PolicyTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.GOME_DATA_COLLECTION.getCode();
    }
}
