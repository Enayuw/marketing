package com.br.marketing.rule.haier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.HaiErRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class HaierCustomerBlackImpl implements AssembleData<BlackDetailDTO> {

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
        HaiErRuleCollectDataImpl.HaiErRuleNecessaryData necessaryData =
                (HaiErRuleCollectDataImpl.HaiErRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser syncUser = necessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if (syncUser == null) {
                return false;
            }

            if ("1".equals(transferSyncUser.getCaseEffective())) {
                return true;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {

        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
        HaiErRuleCollectDataImpl.HaiErRuleNecessaryData necessaryData =
                (HaiErRuleCollectDataImpl.HaiErRuleNecessaryData) context.getRuleNecessaryData();

        MarketingSyncUser syncUser = necessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if (syncUser == null) {
                log.error(String.format("海尔该转化数据没有匹配到原始上传数据 dataId:%d", transferSyncUser.getId()));
                return null;
            }
            BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
            blackDetailDTO.setDataId(String.valueOf(transferSyncUser.getId()));
            blackDetailDTO.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
            return blackDetailDTO;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public String label() {
        return "Haier_OverdueData_CustomerBlack";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.HAI_ER_RULE_DATA_COLLECTION.getCode();
    }
}
