package com.br.marketing.rule.zhongyuan;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.DaasAndConversionData;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongYuanRuleCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 中原消金通话明细推送人工
 * 2023-08-30重构：http://c.100credit.cn/pages/viewpage.action?pageId=125085424
 * @author Guo Zeqiang
 * @dateTime 2023-06-08 16:44
 */
@Service
public class ZhongYuanCallRecordToDass implements AssembleData<DaasAndConversionData> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushDataService pushDataService;

    @Autowired
    ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    private static final Map<String, Pair<String, String>> userTypeMap = new HashMap<>();

    static {
        userTypeMap.put("a1", new Pair<>("3","2"));
        userTypeMap.put("a2", new Pair<>("4","3"));
        userTypeMap.put("b1", new Pair<>("5","2"));
        userTypeMap.put("b2", new Pair<>("6","3"));
    }

    @Override
    public DaasAndConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData =
                (ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodBO> boMap = ruleNecessaryData.getPeriodBOMap();
        SyncUserValidityPeriodBO bo = boMap.get(dto.getCaseNum());
        // 不在有效期
        if (bo == null) {
            return null;
        }

        // 上传表userType
        String syncUserType = bo.getSyncUser().getUserType();
        // 意向等级
        String grade = pushDataService.getStatusByGrade(this.label(), dto.getDetail().getIntentionGrade());
//        if (StringUtils.isEmpty(grade)) {
//            return null;
//        }

        String tcId = tableCreateService.getTcId(dto.getApiCode());
        MarketingTransferSyncUser time =
                marketingTransferSyncUserMapper.getRegisterTimeAndLoginTimeByCreateTimeOrderDesc(tcId, dto.getCaseNum());
        Pair<String, String> pair = userTypeMap.get(grade + syncUserType);
        // syncUserType非（1，2）
        if (pair == null) {
            return null;
        }

        String conditionType = pair.getKey();
        String userType = pair.getValue();

        // 判断开关
        Map<String, Boolean> pushSwitch = marketingCommonConfig.getZhongYuanConditionMap();
        if (!pushSwitch.get("condition_" + conditionType)) {
            return null;
        }

        RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
        realTimeUserDataSoleDTO.setPhoneSaleExtendInfo(buildPhoneSaleExtendInfo(dto, bo, userType));
        realTimeUserDataSoleDTO.setDassSingleImportAdapDTO(buildDassSingleImportAdapSoleDTO(dto, bo, userType, time));
        realTimeUserDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);

        DaasAndConversionData dataDTO = new DaasAndConversionData();
        // 封装dass参数
        dataDTO.setRealTimeUserDataSoleDTO(realTimeUserDataSoleDTO);
        // 封装外呼参数
        dataDTO.setConversionData(buildConversionData(dto, bo));
        return dataDTO;
    }

    private PhoneSaleExtendInfo buildPhoneSaleExtendInfo(CallRecordBO dto, SyncUserValidityPeriodBO bo, String userType) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(dto.getApiCode());
        phoneSaleExtendInfo.setCustNum(dto.getCaseNum());
        phoneSaleExtendInfo.setUserType(dto.getUserType());
        phoneSaleExtendInfo.setAppletDate(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        phoneSaleExtendInfo.setAppletTime(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setTaskId(bo.getSyncUser().getCusBatch());
        phoneSaleExtendInfo.setStatus(pushDataService.getStatusByGrade(this.label(), dto.getDetail().getIntentionGrade()));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setSourceId(dto.getId());
        phoneSaleExtendInfo.setCell(bo.getSyncUser().getCell());
        // 推电销的userType
        phoneSaleExtendInfo.setDxUserType(userType);
        return phoneSaleExtendInfo;
    }

    private ConversionData buildConversionData(CallRecordBO dto, SyncUserValidityPeriodBO bo) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(dto.getId().toString());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(dto.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(dto.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        conversionData.setCid(dto.getCid().toString());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        conversionData.setInversionStatus("0");
        conversionData.setCaseNum(dto.getCaseNum());
        conversionData.setInversionInfo("{}");

        // 去重参数设置
        // 有效期内转化数据以cell为维度仅推送一次
        conversionData.setInitId(dto.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);

        // 有效期设置
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    private DassSingleImportAdapSoleDTO buildDassSingleImportAdapSoleDTO(CallRecordBO dto, SyncUserValidityPeriodBO bo, String userType,
                                                                         MarketingTransferSyncUser time) {
        DassSingleImportDataDTO singleImportDataDTO = new DassSingleImportDataDTO();
        singleImportDataDTO.setName("1");
        singleImportDataDTO.setOrgname("zhongyuanxj");
        singleImportDataDTO.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        singleImportDataDTO.setUid(dto.getCaseNum());

        // userType
        singleImportDataDTO.setUserType(userType);
        // loginTime 和 registerTime。
        singleImportDataDTO.setRegisterTime(formatDate(time.getRegisterTime()));
        singleImportDataDTO.setLoginTime(formatDate(time.getLoginTime()));
        singleImportDataDTO.setSource("30");
        singleImportDataDTO.setId(dto.getId());

        DassSingleImportAdapSoleDTO soleDTO = new DassSingleImportAdapSoleDTO();
        soleDTO.setDassSingleImportDataDTO(singleImportDataDTO);

        // 去重参数设置：7天内单一手机号仅推送一次
        soleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        soleDTO.setSoleDay(7);

        return soleDTO;
    }

    private String formatDate(String str) {
        return StringUtils.isEmpty(str) ? str : str.replace(":000", "");
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof CallRecordBO) {
            // 判断intentionGrade意向等级 非A/B 则结束流程
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String intentionGrade = bo.getDetail().getIntentionGrade();
            Boolean gradeOK = pushDataService.isPushDassWithCallGrade(this.label(), intentionGrade);
            if (gradeOK) {
                // 判断剔除条件：有效期内的全量转化数据根据custNum找有效期内最新的cell且ifApply=1 或 isBlack=1（全局不判断有效期）
                Boolean isExclude = validityPeriodDataService.judgmentMarketingTransferDataInvalidWithValidityPeriod(bo.getApiCode(),
                        bo.getCaseNum());
                if (!isExclude) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongYuan_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_AND_CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGYUAN_DATA_COLLECTION.getCode();
    }
}
