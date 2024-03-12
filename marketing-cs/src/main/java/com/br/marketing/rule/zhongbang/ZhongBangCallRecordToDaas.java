package com.br.marketing.rule.zhongbang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.DaasAndConversionData;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongBangRuleCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
public class ZhongBangCallRecordToDaas implements AssembleData<DaasAndConversionData> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushDataService pushDataService;

    @Autowired
    PhoneSaleExtendServiceImpl phoneSaleExtendService;

    @Override
    public DaasAndConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {

        CallRecordBO dto = (CallRecordBO) transmitFact;
        ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData ruleNecessaryData =
                (ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCallRecordCustomerMap();
        SyncUserValidityPeriodsBO userValidityPeriodsBO = syncUserPeriodMap.get(dto.getCaseNum());
        Map<String, MarketingTransferSyncUser> transferDataMap = ruleNecessaryData.getTransferMap();
        MarketingTransferSyncUser transferSyncUser = transferDataMap.get(dto.getCaseNum());
        DaasAndConversionData dataDTO = new DaasAndConversionData();
        dataDTO.setConversionData(handleConversionData(dto, userValidityPeriodsBO));
        dataDTO.setRealTimeUserDataSoleDTO(handleRealTimeUserData(dto, userValidityPeriodsBO, transferSyncUser));
        return dataDTO;
    }

    private RealTimeUserDataSoleDTO handleRealTimeUserData(CallRecordBO dto, SyncUserValidityPeriodsBO userValidityPeriodsBO
            , MarketingTransferSyncUser transferSyncUser) {
        MarketingSyncUser marketingSyncUser = userValidityPeriodsBO.getSyncUsers().get(0);

        RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
        realTimeUserDataSoleDTO.setDassSingleImportAdapDTO(handlerDaasSingleData(marketingSyncUser, transferSyncUser));

        realTimeUserDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.CALL_RECORD);

        realTimeUserDataSoleDTO.setPhoneSaleExtendInfo(handlerPhoneSaleInfo(dto, marketingSyncUser));

        realTimeUserDataSoleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        realTimeUserDataSoleDTO.setSoleType(1);

        return realTimeUserDataSoleDTO;

    }

    private DassSingleImportAdapSoleDTO handlerDaasSingleData(MarketingSyncUser syncUser
            , MarketingTransferSyncUser transferSyncUser) {
        DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO = new DassSingleImportAdapSoleDTO();
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();
        String reserveField1 = syncUser.getReserveField1();
        dassSingleImportDataDTO.setName("1");
        if (org.springframework.util.StringUtils.hasText(reserveField1)) {
            JSONObject jsonObject = JSON.parseObject(reserveField1);
            String firstName = jsonObject.getString("firstName");
            if (StringUtils.isNotEmpty(firstName)) {
                dassSingleImportDataDTO.setName(firstName.replaceAll("\\*", ""));
            }
        }
        String idCard = BrCipherMaker.getInstance().decode(syncUser.getIdCard());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(idCard)) {
            dassSingleImportDataDTO.setGender(StringUtils.getGenderByIdCard(idCard));
        }
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        //传输明文
        dassSingleImportDataDTO.setPhone(cell);
        dassSingleImportDataDTO.setOrgname("zhongbang");
        dassSingleImportDataDTO.setUid(syncUser.getCustNum());
        dassSingleImportDataDTO.setUserType("1");
        if (transferSyncUser != null) {
            dassSingleImportDataDTO.setRegisterTime(replaceZero(transferSyncUser.getRegisterTime(),""));
            dassSingleImportDataDTO.setLoginTime(replaceZero(transferSyncUser.getLoginTime(), ""));
            dassSingleImportDataDTO.setAuditTime(replaceZero(transferSyncUser.getAuditTime(), ""));
            dassSingleImportDataDTO.setAuditAmount(transferSyncUser.getAuditAmount());
        }
        dassSingleImportDataDTO.setSource("33");
        dassSingleImportAdapSoleDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
        return dassSingleImportAdapSoleDTO;

    }

    private ConversionData handleConversionData(CallRecordBO dto, SyncUserValidityPeriodsBO userValidityPeriodsBO) {
        MarketingSyncUser marketingSyncUser = userValidityPeriodsBO.getSyncUsers().get(0);
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(dto.getId().toString());
        conversionData.setCid(dto.getCid().toString());
        conversionData.setCaseNum(dto.getCaseNum());
        conversionData.setPartnerProcessDate(DateUtils.format(marketingSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        conversionData.setInversionStatus("0");
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        // 去重参数设置
        conversionData.setInitId(dto.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = userValidityPeriodsBO.getBuilders().get(0)
                .addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.CALL_RECORD);
        return conversionData;

    }

    private PhoneSaleExtendInfo handlerPhoneSaleInfo(CallRecordBO dto, MarketingSyncUser marketingSyncUser) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(dto.getApiCode());
        phoneSaleExtendInfo.setCustNum(dto.getCaseNum());
        phoneSaleExtendInfo.setUserType(dto.getUserType());
        phoneSaleExtendInfo.setAppletDate(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        phoneSaleExtendInfo.setAppletTime(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
        phoneSaleExtendInfo.setStatus(dto.getDetail().getIntentionGrade().toLowerCase());
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setSourceId(dto.getId());
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        phoneSaleExtendInfo.setGroupNo(1);
        phoneSaleExtendInfo.setDxUserType("1");
        return phoneSaleExtendInfo;
    }

    /**
     * 2023-08-29 9:31
     * 替换0
     */
    private String replaceZero(String s1, String s2) {
        return org.apache.commons.lang3.StringUtils.isBlank(s1) ? (org.apache.commons.lang3.StringUtils.isBlank(s2) ? s2 : s2.replace(":000", ""))
                : s1.replace(":000", "");
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String intentionGrade = bo.getDetail().getIntentionGrade();
            Boolean intentionA = Boolean.FALSE, intentionB = Boolean.FALSE;
            ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData ruleNecessaryData =
                    (ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap = ruleNecessaryData.getCallRecordCustomerMap();
            if (CollectionUtils.isEmpty(userValidityPeriodsBOMap)) {
                return false;
            }
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = userValidityPeriodsBOMap.get(bo.getCaseNum());
            //有效期判斷
            if (syncUserValidityPeriodsBO == null) {
                return false;
            }
            boolean ASwitch = marketingCommonConfig.getZhongbangStatusTypeMap().get("a").getBooleanValue("switch");
            boolean BSwitch = marketingCommonConfig.getZhongbangStatusTypeMap().get("b").getBooleanValue("switch");
            if (ASwitch) {
                intentionA = "A".equals(intentionGrade);
            }
            if (BSwitch) {
                intentionB = "B".equals(intentionGrade);
            }
            if (intentionA || intentionB) {
                String cell = syncUserValidityPeriodsBO.getSyncUsers().get(0).getCell();
                //去重逻辑判断
                return phoneSaleExtendService.groupRule(bo.getApiCode()
                        , marketingCommonConfig.getZhongbangCellDistributeDay() - 1
                        , cell, 1);
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongBang_CallRecordData_PushDaas";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_AND_CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGBANG_DATA_COLLECTION.getCode();
    }
}
