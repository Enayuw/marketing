package com.br.marketing.rule.zhongbang;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongBangRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 众邦转化推送客服黑名单 业务
 *
 * @author LiXiang
 * @dateTime 2024-04-11
 */
@Service
@Slf4j
public class ZhongBangCustomerBlackListImpl implements AssembleData<BlackDetailDTO> {

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        // 转化接口isBlack=1的custNum取有效期内的上传接口的cell
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData zhongBangContext =
                (ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData) context.getRuleNecessaryData();

        Map<String, SyncUserValidityPeriodsBO> customerMap = zhongBangContext.getCustomerMap();
        SyncUserValidityPeriodsBO userValidityPeriodsBO = customerMap.get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = userValidityPeriodsBO.getSyncUsers().get(0);

        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        blackDetailDTO.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        return blackDetailDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        // 转化接口isBlack=1的custNum取有效期内的上传接口的cell
        if (!(transmitFact instanceof MarketingTransferSyncUser)) {
            return false;
        }
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

        ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData zhongBangContext =
                (ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> customerMap = zhongBangContext.getCustomerMap();
        SyncUserValidityPeriodsBO userValidityPeriodsBO = customerMap.get(transfer.getCustNum());

        if (userValidityPeriodsBO == null) {
            return false;
        }

        JSONObject jo = JSONObject.parseObject(transfer.getReserveField1());
        if (jo == null) {
            return false;
        }
        String isBlack = jo.getString("isBlack");
        if (StringUtils.isEmpty(isBlack) || !"1".equals(isBlack)) {
            return false;
        }
        return true;
    }

    @Override
    public String label() {
        return "ZhongBang_TransferData_CustomerBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGBANG_DATA_COLLECTION.getCode();
    }
}
