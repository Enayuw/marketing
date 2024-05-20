package com.br.marketing.rule.zhongyou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongYouRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Description ZhongYouCustomerTransferImpl
 * @Author hong.chen
 * @CreateTime 2023/08/07
 */
@Service
@Slf4j
public class ZhongYouCustomerTransferImpl implements AssembleData<ConversionData> {

    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        log.warn("中邮推客服转化,apicode={}", transfer.getApiCode());
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());

        ZhongYouRuleCollectDataImpl.ZhongYouRuleNecessaryData contextRuleNecessaryData = (ZhongYouRuleCollectDataImpl.ZhongYouRuleNecessaryData
                ) context.getRuleNecessaryData();

        // 有效期设置
        conversionData.setExpireDate(contextRuleNecessaryData.getExpireDate());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));

        // phone
        MarketingSyncUser syncUser = contextRuleNecessaryData.getSyncUser();
        if (syncUser != null) {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
        } else {
            conversionData.setPhone("");
        }

        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        conversionData.setCaseNum(transfer.getCustNum());
        // inversionInfo(必填)
        conversionData.setInversionInfo("{}");

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        log.warn("中邮推客服转化:isNeedAssemble");
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                String applyLentTime = json.getString("applyLentTime");
                String pushTime = json.getString("pushTime");
                if (StringUtils.isEmpty(applyLentTime) || StringUtils.isEmpty(pushTime)) {
                    return false;
                }

                // yyyy-MM-dd
                SimpleDateFormat format = new SimpleDateFormat(DateHelper.LINE_DATE_FORMAT);
                Date date1;
                try {
                    date1 = format.parse(applyLentTime);
                } catch (Exception e) {
                    // yyyyMMdd
                    format = new SimpleDateFormat(DateHelper.SHORT_DATE_FORMAT);
                    try {
                        date1 = format.parse(applyLentTime);
                    } catch (Exception e1) {
                        log.error("中邮推客服转化,applyLentTime日期格式解析失败,转化数据id：{}", transfer.getId(), e1);
                        return false;
                    }
                }

                Date date2;
                try {
                    date2 = format.parse(pushTime);
                } catch (Exception e) {
                    // yyyyMMdd
                    format = new SimpleDateFormat(DateHelper.SHORT_DATE_FORMAT);
                    try {
                        date2 = format.parse(pushTime);
                    } catch (Exception e1) {
                        log.error("中邮推客服转化,pushTime日期格式解析失败,转化数据id：{}", transfer.getId(), e1);
                        return false;
                    }
                }

                // 若applyLentTime小于pushTime，则不推送
                if (date1.before(date2)) {
                    return false;
                }

                // 判断是否在有效期范围[t,t+45]
                Set<String> custNumSet = new HashSet<>();
                custNumSet.add(transfer.getCustNum());
                Map<String, SyncUserValidityPeriodBO> periodBOMap =
                        transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSet, transfer.getApiCode(), new Date());
                SyncUserValidityPeriodBO bo = periodBOMap.get(transfer.getCustNum());
                if (bo == null) {
                    return false;
                }

                ZhongYouRuleCollectDataImpl.ZhongYouRuleNecessaryData contextRuleNecessaryData =
                        (ZhongYouRuleCollectDataImpl.ZhongYouRuleNecessaryData
                                ) context.getRuleNecessaryData();
                // 将上传数据保存到上下文
                contextRuleNecessaryData.setSyncUser(bo.getSyncUser());
                // 将失效时间保存到上下文
                PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
                contextRuleNecessaryData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());

                flag = Boolean.TRUE;
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "ZhongYou_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGYOU_DATA_COLLECTION.getCode();
    }
}
