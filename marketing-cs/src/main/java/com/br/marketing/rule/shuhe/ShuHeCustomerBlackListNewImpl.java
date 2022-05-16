package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 数禾转化推送客服黑名单 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
@Slf4j
public class ShuHeCustomerBlackListNewImpl implements AssembleData<BlackDetailDTO> {

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        JSONObject json = JSON.parseObject(transfer.getReserveField1());
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        blackDetailDTO.setExpireDate(json.getString("clc_usr_max_dx_rrt_end"));
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
                    String reserveField1 = transfer.getReserveField1();
                    if (StringUtils.hasText(reserveField1)){
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        String clcUsrMaxDxRrtEnd = jsonObject.getString("clc_usr_max_dx_rrt_end");
                        if (StringUtils.hasText(clcUsrMaxDxRrtEnd)) {
                            shuHeContext.setContinueJudgeRule(false);
                            bool = Boolean.TRUE;
                        }
                    }
                }
            }
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_0_TransferData_CustomerBlackList";
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
