package com.br.marketing.rule.qifu;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.QiFuRuleCollectDataImpl;
import com.br.marketing.context.impl.ZhongBangRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 描述：： 360 转化数据推客服过滤
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName QiFuTransferDataToCustomerFilter
 * @author: it-yml
 * @create: 2023-09-27 16:48
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class QiFuTransferDataToCustomerFilter implements AssembleData<ConversionData> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        QiFuRuleCollectDataImpl.QiFuRuleNecessaryData ruleNecessaryData =
                (QiFuRuleCollectDataImpl.QiFuRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = syncUserPeriodMap.get(transfer.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        conversionData.setPhone(BrCipherMaker.getInstance().decode( syncUserValidityPeriodsBO.getSyncUsers().get(0).getCell()));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            String apiCode = marketingCommonConfig.getQiFuApiCodeToCustomerMap().get("QiFu_TransferData_To_CustomerFilter");
            context.setApiCode(apiCode);
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String custNum = transfer.getCustNum();
            if("1".equals(transfer.getTransformTime())){
                log.info("{},【TransformTime】为1",custNum);
                return false;
            }
            // 1. 有效期判断
            // 2. 公共方法调用
            QiFuRuleCollectDataImpl.QiFuRuleNecessaryData ruleNecessaryData =
                    (QiFuRuleCollectDataImpl.QiFuRuleNecessaryData) context.getRuleNecessaryData();

            Map<String, SyncUserValidityPeriodsBO> customerMap = ruleNecessaryData.getCustomerMap();

            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = customerMap.get(custNum);
            if(syncUserValidityPeriodsBO==null){
                log.info("{},数据不在有效期范围内！",custNum);
                return false;
            }
            String loginTime = transfer.getLoginTime();
            String applyDt = transfer.getApplyDt();
            if(StringUtils.isEmpty(loginTime) && StringUtils.isEmpty( applyDt)){
                log.info("{},【loginTime,applyDt】为空！",custNum);
                return false;
            }
            if(!StringUtils.isEmpty(loginTime)){
                LocalDate localLoginDate = LocalDate.parse(loginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                List<PeriodOfValidityBO.Builder> builders = syncUserValidityPeriodsBO.getBuilders();
                for (int i = 0; i < builders.size(); i++) {
                    PeriodOfValidityBO.Builder builder = builders.get(i);
                    String startOfDayTimeStr = builder.addDateString().addOfDayTimeStrString().builder().getStartOfDayTimeStr();
                    LocalDate localStartOfDayTimeStr = LocalDate.parse(startOfDayTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if(localLoginDate.now().isAfter(localStartOfDayTimeStr.now())){
                        return true;
                    }
                }
            }else {
                LocalDate localApplyDt = LocalDate.parse(applyDt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                List<PeriodOfValidityBO.Builder> builders = syncUserValidityPeriodsBO.getBuilders();
                for (int i = 0; i < builders.size(); i++) {
                    PeriodOfValidityBO.Builder builder = builders.get(i);
                    String startOfDayTimeStr = builder.addDateString().addOfDayTimeStrString().builder().getStartOfDayTimeStr();
                    LocalDate localStartOfDayTimeStr = LocalDate.parse(startOfDayTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if(localApplyDt.now().isAfter(localStartOfDayTimeStr.now())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "QiFu_TransferData_To_CustomerFilter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.QIFU360_DATA_COLLECTION.getCode();
    }
}
