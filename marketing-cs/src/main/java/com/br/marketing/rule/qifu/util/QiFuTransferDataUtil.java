package com.br.marketing.rule.qifu.util;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.impl.QiFuRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 描述：： 360 数据公共方法
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName QiFuTransferDataUtil
 * @author: it-yml
 * @create: 2023-10-08 14:50
 * @Version 1.0
 * --------------------------------------
 **/
@Slf4j
public class QiFuTransferDataUtil {

    public static ConversionData getConversionData(MarketingTransferSyncUser transmitFact, ProcessHandlerContext context) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transmitFact.getId().toString());
        conversionData.setCid(transmitFact.getCid());
        conversionData.setCaseNum(transmitFact.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(transmitFact.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        QiFuRuleCollectDataImpl.QiFuRuleNecessaryData ruleNecessaryData =
                (QiFuRuleCollectDataImpl.QiFuRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = syncUserPeriodMap.get(transmitFact.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        conversionData.setPhone(BrCipherMaker.getInstance().decode( syncUserValidityPeriodsBO.getSyncUsers().get(0).getCell()));
        // 去重参数设置
        conversionData.setInitId(transmitFact.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    public static SyncUserValidityPeriodsBO getSyncUserValidityPeriodsBO(ProcessHandlerContext context, String custNum) {
        QiFuRuleCollectDataImpl.QiFuRuleNecessaryData ruleNecessaryData =
                (QiFuRuleCollectDataImpl.QiFuRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> customerMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = customerMap.get(custNum);
        if(syncUserValidityPeriodsBO==null){
            log.info("{},数据不在有效期",custNum);
        }
        return syncUserValidityPeriodsBO;
    }

    public static boolean isNeedAssmble(MarketingTransferSyncUser transfer, SyncUserValidityPeriodsBO syncUserValidityPeriodsBO) {
        String custNum = transfer.getCustNum();
        if ("1".equals(transfer.getTransformTime())) {
            log.info("{},【TransformTime】为1", custNum);
            return false;
        }
        if (syncUserValidityPeriodsBO == null) {
            log.info("{},数据不在有效期范围内！", custNum);
            return false;
        }
        return true;
    }

    public static boolean isRuleAssmble(String ruleDate,  String custNum, SyncUserValidityPeriodsBO syncUserValidityPeriodsBO) {
        if (StringUtils.isEmpty(ruleDate)) {
            log.info("{},【ruleDate】为空！", custNum);
            return false;
        }
        LocalDate localRuleDate = LocalDate.parse(ruleDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<PeriodOfValidityBO.Builder> builders = syncUserValidityPeriodsBO.getBuilders();
        for (PeriodOfValidityBO.Builder builder : builders) {
            String startOfDayTimeStr = builder.addDateString().addOfDayTimeStrString().builder().getStartOfDayTimeStr();
            LocalDate localStartOfDayTimeStr = LocalDate.parse(startOfDayTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (localRuleDate.isAfter(localStartOfDayTimeStr)) {
                return true;
            }
        }
        return false;
    }

}
