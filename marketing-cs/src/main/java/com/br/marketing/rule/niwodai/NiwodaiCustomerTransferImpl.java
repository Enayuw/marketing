package com.br.marketing.rule.niwodai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.NiwodaiRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * D20230314你我贷自动化过滤-3710064 业务
 * http://c.100credit.cn/pages/viewpage.action?pageId=103562399
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/23 9:10
 */
@Service
@Slf4j
public class NiwodaiCustomerTransferImpl implements AssembleData<ConversionData> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);
    private static final List<String> USER_TYPES = Arrays.asList("0", "1", "3", "7", "9");

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus(data.getInversionStatus());
        Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        PeriodOfValidityBO.Builder builder = bo.getBuilders().get(0);
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
        // 有效期设置
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                JSONObject jsonObject;
                try {
                    jsonObject = JSONObject.parseObject(reserveField1);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    return false;
                }
                //1.有效期判断
                NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                        (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
                if (data.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) == null) {
                    return false;
                }
                //2.规则过滤并赋值inversionStatus
                String inversionStatus = getInversionStatus(transfer.getUserType(), jsonObject);
                if (StringUtils.isEmpty(inversionStatus)) {
                    return false;
                }
                data.setInversionStatus(inversionStatus);
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤规则，获得inversionStatus
     * @param userType
     * @param reserveField1
     */
    private String getInversionStatus(String userType, JSONObject reserveField1) {
        String inversionStatus = "0";
        if (Optional.ofNullable(reserveField1.getInteger("F")).isPresent() &&
                reserveField1.getInteger("F") == 1) {
            return inversionStatus;
        }
        if (Optional.ofNullable(reserveField1.getInteger("B")).isPresent()
                && reserveField1.getInteger("B") != 0 && USER_TYPES.contains(userType)) {
            return inversionStatus;
        }
        if (Optional.ofNullable(reserveField1.getInteger("C")).isPresent() &&
                reserveField1.getInteger("C") == 1 && USER_TYPES.contains(userType)) {
            return inversionStatus;
        }
        if ((Optional.ofNullable(reserveField1.getInteger("C")).isPresent()
                && reserveField1.getInteger("C") == 1)
                && (Optional.ofNullable(reserveField1.getInteger("D")).isPresent()
                && reserveField1.getInteger("D") == 0) && userType.equals("20")) {
            return inversionStatus;
        }
        if (Optional.ofNullable(reserveField1.getInteger("H")).isPresent()
                && reserveField1.getInteger("H") == 1) {
            inversionStatus = "2";
            return inversionStatus ;
        }
        inversionStatus = "";
        return inversionStatus;
    }

    @Override
    public String label() {
        return "niwodai_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.NIWODAI_DATA_COLLECTION.getCode();
    }
}
