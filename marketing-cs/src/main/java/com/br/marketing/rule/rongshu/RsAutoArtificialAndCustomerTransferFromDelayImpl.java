package com.br.marketing.rule.rongshu;

import java.util.Date;
import java.math.BigDecimal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.context.impl.PPDCollectDataImpl;
import com.br.marketing.service.IRongShuPushDaasService;
import com.google.api.client.json.Json;
import com.google.common.collect.Lists;
import com.br.marketing.client.dassservice.input.IbuReqDTO.Datum;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.entity.PhoneSaleExtendInfo;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.ibu.IbuAdapDTO;
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
public class RsAutoArtificialAndCustomerTransferFromDelayImpl implements AssembleData<IbuAdapDTO> {


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

    @Autowired
    IRongShuPushDaasService iRongShuPushDaasService;


    @Override
    public IbuAdapDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

        RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        IbuAdapDTO ibuAdapDTO = new IbuAdapDTO();
        Datum datum = new Datum();
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        ConversionData conversionData = new ConversionData();
        ibuAdapDTO.setDatum(datum);
        ibuAdapDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
        ibuAdapDTO.setConversionData(conversionData);
        ibuAdapDTO.setPushType("a");

        datum.setId(transfer.getId());
        datum.setUid(transfer.getCustNum());
        datum.setUserType("D");
        datum.setUserCode(transfer.getCustNum());
        datum.setUserName("1");
        datum.setPhone(cell);
        datum.setSource("100");
        datum.setGoalsApp("01");
        if (StringUtils.isNotBlank(marketingSyncUser.getReserveField1())) {
            JSONObject json = JSON.parseObject(marketingSyncUser.getReserveField1());
            String tid = json.getString("tid");
            String operateType = json.getString("operateType");
            String planId = json.getString("planId");
            JSONObject reserve = new JSONObject();
            if (tid != null) {
                reserve.put("tid", tid);
            }
            if (operateType != null) {
                reserve.put("operateType", operateType);
            }
            if (StringUtils.isNotBlank(planId)) {
                datum.setPlanId(Integer.valueOf(planId));
            }
            datum.setReserveField1(reserve.toJSONString());
        }

        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setInversionStatus("0");
        conversionData.setPhone(cell);
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(tableCreateService.getCId(context.getApiCode()));
        conversionData.setExpireDate(marketingCommonConfig.getRsTransferDataToCustomerExpireDate());
        if (!org.springframework.util.StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }

        phoneSaleExtendInfo.setApiCode(context.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(transfer.getRequestData());
        phoneSaleExtendInfo.setAppletTime(transfer.getRequestTime());
        phoneSaleExtendInfo.setStatus("a");
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfo.setRedundancyField(JSON.toJSONString(datum));
        phoneSaleExtendInfo.setInterfaceType((marketingCommonConfig.getRongShuPushNewIbuSwitch() != null && !marketingCommonConfig.getRongShuPushNewIbuSwitch())
                ? 2 : 1);
        return ibuAdapDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MqFact mqFact = context.getMqFact();
        Integer isDelay = mqFact.getIsDelay();
        if (isDelay == null || isDelay != 1) {
            return false;
        }
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            if (marketingSyncUser == null) {
                return false;
            }
            return !iRongShuPushDaasService.isFilter(context.getApiCode(), transfer.getCustNum(), tableCreateService.getTcId(context.getApiCode()));
        }
        return false;
    }


    @Override
    public String label() {
        return "RongShu_TransferData_ArtificialAndCustomerBatch";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_IBU_BATCH_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }
}
