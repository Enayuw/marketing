package com.br.marketing.rule.zhongyuan;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongYuanRuleCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中原消金通话明细推送人工
 *
 * @author Guo Zeqiang
 * @dateTime 2023-06-08 16:44
 */
@Service
public class ZhongYuanCallRecordToDass implements AssembleData<BatchRealTimeUserDataDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushDataService pushDataService;

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData =
                (ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCallRecordCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, dto.getCaseNum());
        if (marketingSyncUser == null) {
            return null;
        }
        BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
        dataDTO.setDassImportDataDTO(packageDassImportData(dto, marketingSyncUser));
        dataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(dto, marketingSyncUser));
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
        phoneSaleExtendInfo.setStatus(pushDataService.getStatusByGrade(this.label(),dto.getDetail().getIntentionGrade()));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setSourceId(dto.getId());
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        return phoneSaleExtendInfo;
    }

    private DassImportDataDTO packageDassImportData(CallRecordBO dto, MarketingSyncUser marketingSyncUser) {
        String phone = AESUtil.aesEncrypty(BrCipherMaker.getInstance().decode(
                marketingSyncUser.getCell()), aesKey);
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setName("1");
        batchImportData.setOrgname("zhongyuanxj");
        batchImportData.setPhone(phone);
        batchImportData.setUserType("2");
        batchImportData.setSource("30");
        batchImportData.setUid(dto.getCaseNum());
        batchImportData.setId(dto.getId());
        return batchImportData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String intentionGrade = bo.getDetail().getIntentionGrade();
            return pushDataService.isPushDassWithCallGrade(this.label(),intentionGrade);
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongYuan_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGYUAN_DATA_COLLECTION.getCode();
    }
}
