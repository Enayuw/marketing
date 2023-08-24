package com.br.marketing.rule.zhongbang;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongBangRuleCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 众邦通话明细推送人工
 *
 * @author zhen.Li
 * @dateTime 2023-08-01 16:44
 */
@Service
public class ZhongBangCallRecordToDaas implements AssembleData<BatchRealTimeUserDataDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushDataService pushDataService;


    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData ruleNecessaryData =
                (ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData) context.getRuleNecessaryData();
        /*Map<String, SyncUserValidityPeriodBO>  customerMap = ruleNecessaryData.getCallRecordCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, dto.getCaseNum());
        if (marketingSyncUser == null) {
            return null;
        }
        BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
        dataDTO.setDassImportDataDTO(packageDassImportData(dto, marketingSyncUser));
        dataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(dto, marketingSyncUser));*/
        return dataDTO;
    }

    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(CallRecordBO dto, MarketingSyncUser marketingSyncUser) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(dto.getApiCode());
        phoneSaleExtendInfo.setCustNum(dto.getCaseNum());
        phoneSaleExtendInfo.setUserType(dto.getUserType());
        phoneSaleExtendInfo.setAppletDate(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        phoneSaleExtendInfo.setAppletTime(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
        phoneSaleExtendInfo.setStatus(pushDataService.getStatusByGrade(this.label(), dto.getDetail().getIntentionGrade()));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setSourceId(dto.getId());
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        return phoneSaleExtendInfo;
    }

    private DassImportDataDTO packageDassImportData(CallRecordBO dto, MarketingSyncUser marketingSyncUser) {
        String phone = AESUtil.aesEncrypty(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()), aesKey);
        String firstName = "";
        if (StringUtils.isNotEmpty(marketingSyncUser.getReserveField1())) {
            firstName = JSON.parseObject(marketingSyncUser.getReserveField1()).getString("firstName");
        }
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setName(StringUtils.isNotEmpty(firstName) ? firstName : "1");
        batchImportData.setOrgname("zhongbang");
        batchImportData.setPhone(phone);
        batchImportData.setUserType("1");
        batchImportData.setSource("33");
        batchImportData.setUid(dto.getCaseNum());
        batchImportData.setId(dto.getId());
        return batchImportData;

    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof CallRecordBO) {
            boolean ASwitch = marketingCommonConfig.getZhongbangStatusTypeMap().get("a").getBooleanValue("switch");
            boolean BSwitch = marketingCommonConfig.getZhongbangStatusTypeMap().get("b").getBooleanValue("switch");
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String intentionGrade = bo.getDetail().getIntentionGrade();
            return pushDataService.isPushDassWithCallGrade(this.label(), intentionGrade);
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongBang_CallRecordData_PushDaas";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGBANG_DATA_COLLECTION.getCode();
    }
}
