//package com.br.marketing.rule.zhongyuan;
//
//import com.br.common.util.BrCipherMaker;
//import com.br.common.util.DateUtils;
//import com.br.marketing.bo.SyncUserValidityPeriodBO;
//import com.br.marketing.client.DaasAndConversionData;
//import com.br.marketing.client.dassservice.input.DassImportDataDTO;
//import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
//import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
//import com.br.marketing.client.robotaiapi.input.ConversionData;
//import com.br.marketing.common.utils.AESUtil;
//import com.br.marketing.context.ProcessHandlerContext;
//import com.br.marketing.context.RuleDataCollectionEnum;
//import com.br.marketing.context.impl.ZhongYuanRuleCollectDataImpl;
//import com.br.marketing.dto.customer.CallRecordBO;
//import com.br.marketing.entity.MarketingSyncUser;
//import com.br.marketing.entity.PhoneSaleExtendInfo;
//import com.br.marketing.rule.AssembleData;
//import com.br.marketing.service.PushDataService;
//import com.br.marketing.service.ValidityPeriodDataService;
//import com.br.marketing.speedconfig.MarketingCommonConfig;
//import com.br.marketing.strategy.InterfaceHandlerEnum;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//import java.util.Map;
//
///**
// * 中原消金通话明细推送人工
// *
// * @author Guo Zeqiang
// * @dateTime 2023-06-08 16:44
// */
//@Service
//public class ZhongYuanCallRecordToDass implements AssembleData<BatchRealTimeUserDataDTO> {
//
//    @Value("${api.dass.aesKey:00}")
//    private String aesKey;
//
//    @Autowired
//    MarketingCommonConfig marketingCommonConfig;
//
//    @Autowired
//    PushDataService pushDataService;
//
//    @Autowired
//    ValidityPeriodDataService validityPeriodDataService;
//
//    @Override
//    public DaasAndConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
//        CallRecordBO dto = (CallRecordBO) transmitFact;
//        ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData =
//                (ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData) context.getRuleNecessaryData();
////        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCallRecordCustomerMap();
//        Map<String, SyncUserValidityPeriodBO> boMap = ruleNecessaryData.getPeriodBOMap();
//        SyncUserValidityPeriodBO bo = boMap.get(dto.getCaseNum());
//
//        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, dto.getCaseNum());
//        if (marketingSyncUser == null) {
//            return null;
//        }
////        BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
////        dataDTO.setDassImportDataDTO(packageDassImportData(dto, marketingSyncUser));
////        dataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(dto, marketingSyncUser));
//        DaasAndConversionData dataDTO = new DaasAndConversionData();
//        dataDTO.setConversionData();
//        dataDTO.setRealTimeUserDataSoleDTO();
//        return dataDTO;
//    }
//
//    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(CallRecordBO dto, MarketingSyncUser marketingSyncUser) {
//        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
//        phoneSaleExtendInfo.setApiCode(dto.getApiCode());
//        phoneSaleExtendInfo.setCustNum(dto.getCaseNum());
//        phoneSaleExtendInfo.setUserType(dto.getUserType());
//        phoneSaleExtendInfo.setAppletDate(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
//                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
//        phoneSaleExtendInfo.setAppletTime(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
//                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
//        phoneSaleExtendInfo.setStatus(pushDataService.getStatusByGrade(this.label(),dto.getDetail().getIntentionGrade()));
//        phoneSaleExtendInfo.setPStatus(1);
//        phoneSaleExtendInfo.setCreateTime(new Date());
//        phoneSaleExtendInfo.setPushDxTime(new Date());
//        phoneSaleExtendInfo.setSourceId(dto.getId());
//        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
//        return phoneSaleExtendInfo;
//    }
//
//    private DassImportDataDTO packageDassImportData(CallRecordBO dto, MarketingSyncUser marketingSyncUser) {
//        String phone = AESUtil.aesEncrypty(BrCipherMaker.getInstance().decode(
//                marketingSyncUser.getCell()), aesKey);
//        DassImportDataDTO batchImportData = new DassImportDataDTO();
//        batchImportData.setName("1");
//        batchImportData.setOrgname("zhongyuanxj");
//        batchImportData.setPhone(phone);
//        batchImportData.setUserType("2");
//        batchImportData.setSource("30");
//        batchImportData.setUid(dto.getCaseNum());
//        batchImportData.setId(dto.getId());
//        return batchImportData;
//    }
//
//    private ConversionData buildConversionData(){
//        ConversionData conversionData = new ConversionData();
//        conversionData.setDataId(transferSyncUser.getId().toString());
//        conversionData.setCid(transferSyncUser.getCid());
//        conversionData.setCaseNum(transferSyncUser.getCustNum());
//        conversionData.setInversionStatus("2");
//        if (!StringUtils.isEmpty(transferSyncUser.getCreateTime())){
//            conversionData.setPartnerProcessDate(DateUtils.format(transferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
//        }
//
//        return conversionData;
//    }
//
//    private RealTimeUserDataSoleDTO buildRealTimeUserDataSoleDTO(){
//
//    }
//
//    @Override
//    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
//        if (transmitFact instanceof CallRecordBO) {
//            // 判断intentionGrade意向等级 非A/B 则结束流程
//            CallRecordBO bo = (CallRecordBO) transmitFact;
//            String intentionGrade = bo.getDetail().getIntentionGrade();
//            Boolean gradeOK = pushDataService.isPushDassWithCallGrade(this.label(), intentionGrade);
//            if (gradeOK) {
//                // 判断剔除条件：有效期内的全量转化数据根据custNum找有效期内最新的cell且ifApply=1 或 isBlack=1（全局不判断有效期）
//                Boolean isExclude = validityPeriodDataService.judgmentMarketingTransferDataInvalidWithValidityPeriod(bo.getApiCode(),
//                        bo.getCaseNum());
//                if (isExclude) {
//                    return false;
//                }
//
//                return true;
//            }
//
//
//        }
//        return false;
//    }
//
//    @Override
//    public String label() {
//        return "ZhongYuan_CallRecordData_PhoneSale";
//    }
//
//    @Override
//    public Integer dataDirection() {
//        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_AND_CUSTOMER_TRANSFER_SOLE.getCode();
//    }
//
//    @Override
//    public Integer ruleDataCollection() {
//        return RuleDataCollectionEnum.ZHONGYUAN_DATA_COLLECTION.getCode();
//    }
//}
