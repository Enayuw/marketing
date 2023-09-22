package com.br.marketing.rule.xiecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @Description : 携程客服转化规则 cpa
 * https://c.100credit.cn/pages/viewpage.action?pageId=125100190
 * ---------------------------------
 * @Author : 张广超
 * @Date : Create in 2023/09/19 10:28
 * cpa 3710090 转化数据推客服 判断110 且无106 的数据推
 */
@Service
@Slf4j
public class XieChengCustomerTransferCpaToDelayImpl implements AssembleData<MqFact> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        Set<String> set = new HashSet<>();
        set.add("XieCheng_TransferData_CPA_From_Delay_CustomerTransfer");
        mqFact.setSourceId(transfer.getId());
        mqFact.setIncludeRules(set);
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        log.warn("进入通用队列.......");
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer convType = json.getInteger("convType");
                // 从配置中心获取convType
                Set<String> convTypeSet = marketingCommonConfig.getPushConvTypeConfig().get(transfer.getApiCode()).keySet();
                flag = !StringUtils.isEmpty(convType) && convTypeSet.contains(convType.toString());
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "XieCheng_TransferData_CPA_To_Delay_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
