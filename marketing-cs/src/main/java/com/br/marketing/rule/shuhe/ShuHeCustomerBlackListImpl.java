package com.br.marketing.rule.shuhe;

import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * 数禾转化推送客服黑名单 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
@Slf4j
public class ShuHeCustomerBlackListImpl implements AssembleData<BlackDetailDTO> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    public static final DateTimeFormatter YMHDMS = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        HashMap<String, Integer> shuhePushBlackDay = marketingCommonConfig.getShuhePushBlackDay();
        Integer blackDays = 30;
        if(shuhePushBlackDay!=null){
            blackDays = shuhePushBlackDay.getOrDefault("customerBlack", 30);
        }
        String endTime = LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59)
                .plusDays(blackDays).format(YMHDMS);
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        blackDetailDTO.setExpireDate(endTime);
        blackDetailDTO.setPhone(caseShuheUser.getCell());
        return blackDetailDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            Integer isDelay = context.getMqFact().getIsDelay();
            if (isDelay == null || isDelay != 1) {
                ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                        (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
                if (shuHeContext.isContinueJudgeRule()) {
                    BaseUserType baseUserType = shuHeContext.getBaseUserType();
                    CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
                    if (shuHeContext.getNonBlackListCount() == 0 && baseUserType.isBlack(caseShuheUser)) {
                        shuHeContext.setContinueJudgeRule(false);
                        bool = Boolean.TRUE;
                    }
                }
            }
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_1_TransferData_CustomerBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }
}
