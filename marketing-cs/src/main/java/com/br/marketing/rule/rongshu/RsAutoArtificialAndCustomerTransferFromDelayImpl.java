package com.br.marketing.rule.rongshu;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Service
public class RsAutoArtificialAndCustomerTransferFromDelayImpl implements AssembleData<MqFact> {


    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    IPeriodOfValidityService iPeriodOfValidityService;

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(transfer.getId());
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            if (marketingSyncUser == null) {
                return false;
            }
            String appletDate = marketingSyncUser.getAppletDate();
            //todo 判断剔除
            return true;

        }
        return false;

    }


    @Override
    public String label() {
        return "RongShu_TransferData_ArtificialAndCustomerBatch";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }
}
