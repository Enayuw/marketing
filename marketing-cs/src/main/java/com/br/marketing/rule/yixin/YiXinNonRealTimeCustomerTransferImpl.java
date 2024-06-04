package com.br.marketing.rule.yixin;

import java.util.Set;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.enums.SoleFieldEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;

/**
 * 非实时转化数据推送客服
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/29 14:45
 */
@Service
public class YiXinNonRealTimeCustomerTransferImpl implements AssembleData<ConversionData> {

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
            (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData)context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO userValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        /*if (userValidityPeriodsBO == null) {
            return null;
        }*/
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setTransformType("2");
        conversionData.setInversionStatus("0");
        String datetimePattern = "yyyy-MM-dd HH:mm:ss";
        conversionData.setPartnerProcessDate(StringUtils.isEmpty(transfer.getCreateTime())
                ? null : DateUtils.format(transfer.getCreateTime(), datetimePattern));
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重设置
        conversionData.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        conversionData.setSoleType(1);
        // 有效期设置 transformType非1的非实时数据传输生效截止时间点
        PeriodOfValidityBO periodOfValidityBO = userValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        final Set<String> includeRules = context.getMqFact().getIncludeRules();
        if (!CollectionUtils.isEmpty(includeRules) && includeRules.contains(label())) {
            // 该标识只为调度任务查询数据使用，非数据ID
            context.setTransferInfoId(-1L);
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "YiXin_NonRealTime_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
