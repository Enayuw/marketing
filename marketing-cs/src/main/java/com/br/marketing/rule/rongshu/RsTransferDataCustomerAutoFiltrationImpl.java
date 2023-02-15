package com.br.marketing.rule.rongshu;

import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * 榕树转化数据自动过滤推客服
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @Date 2023/2/13 17:52
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RsTransferDataCustomerAutoFiltrationImpl implements AssembleData<ConversionData> {


    private final static String MD5 = "md5";
    private final static String TYPE = "cell";

    private final static String INVERSIONSTATUS="0";



    private final MarketingCommonConfig marketingCommonConfig;


    private final IPeriodOfValidityService iPeriodOfValidityService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setCid(transfer.getCid());
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setExpireDate(marketingCommonConfig.getRsTransferDataToCustomerExpireDate());
        conversionData.setInversionStatus(INVERSIONSTATUS);
        RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
        if (marketingSyncUser != null) {
            conversionData.setPhone( RpcClientProxy.decode(marketingSyncUser.getCell(),TYPE ,MD5,""));
        }

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            //用户非空判断
            if (marketingSyncUser == null) {
                return false;
            }
            // 有效期判断
            String appletDate = marketingSyncUser.getAppletDate();
            if (iPeriodOfValidityService.isExpire(appletDate,marketingCommonConfig.getRsValidityDay(),null)) {
                return false;
            }
            // unlenAmount 金额判断
            int rsUnlentAmount = marketingCommonConfig.getRsUnlentAmount() == null ? 1000 : marketingCommonConfig.getRsUnlentAmount();
            Double unlentAmount = StringUtils.isNotBlank(transfer.getUnlentAmount())
                    ? Double.valueOf(transfer.getUnlentAmount())
                    : new Double(0);
            //userType =4 || userType =5 || unlentAmount < 10000
            return transfer.getUserType().equals("4") || transfer.getUserType().equals("5") || unlentAmount < rsUnlentAmount;
        }
        return false;
    }

    @Override
    public String label() {
        return "RongShu_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
