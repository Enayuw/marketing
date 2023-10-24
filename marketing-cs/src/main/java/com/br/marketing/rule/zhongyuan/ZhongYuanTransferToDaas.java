package com.br.marketing.rule.zhongyuan;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongYuanRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 废弃：2023-08-29 http://c.100credit.cn/pages/viewpage.action?pageId=125085409
 * 中原消金断点数据推送人工
 *
 * @author zhen.Li
 * @dateTime 2023-06-08 17:44
 */
@Service
@Slf4j
public class ZhongYuanTransferToDaas implements AssembleData<BatchRealTimeUserDataDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;


    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();

        ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData =
                (ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
        if (marketingSyncUser == null) {
            return null;
        }
        batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer, marketingSyncUser));
        batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(transfer, marketingSyncUser));
        return batchRealTimeUserDataDTO;
    }

    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer, MarketingSyncUser marketingSyncUser) {
        LocalDateTime localDateTime = transfer.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(transfer.getType());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfo.setStatus("b");
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        return phoneSaleExtendInfo;


    }

    private DassImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer, MarketingSyncUser marketingSyncUser) {

        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setName("1");
        batchImportData.setOrgname("zhongyuanxj");
        batchImportData.setPhone(phone);
        batchImportData.setUserType("1");
        batchImportData.setSource("30");
        batchImportData.setId(transfer.getId());
        batchImportData.setUid(transfer.getCustNum());
        return batchImportData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            if (StringUtils.isNotBlank(transfer.getRegisterTime()) && ("1".equals(transfer.getIfLogin()))) {
                return true;
            }

        }
        return false;
    }


    @Override
    public String label() {
        return "ZhongYuan_TransferData_ArtificialBatch";
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
