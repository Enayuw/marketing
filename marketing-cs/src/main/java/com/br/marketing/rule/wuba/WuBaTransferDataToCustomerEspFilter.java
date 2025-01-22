package com.br.marketing.rule.wuba;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.WuBaRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author peng.kang
 * @description: T日转化数据中earlyScreenPass=0的custNum过滤
 * @date 2025/1/14
 */
@Service
@Slf4j
public class WuBaTransferDataToCustomerEspFilter implements AssembleData<ConversionData> {
    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(marketingTransferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(marketingTransferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        conversionData.setInversionStatus("2");
        String phone = "";
        try {
            phone = RpcClientProxy.decode(marketingTransferSyncUser.getCustNum(), "cell", "md5", "");
        } catch (Exception e) {
            String title = "58新客转化推送客服custNum解密失败!";
            String msg = marketingTransferSyncUser.getCustNum();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            return null;
        }

        conversionData.setPhone(phone);
        conversionData.setExpireDate(DateUtil.today() + " 23:59:59");
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(marketingTransferSyncUser, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (!(transmitFact instanceof MarketingTransferSyncUser)) {
            return false;
        }
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String reserveField = transfer.getReserveField1();
        if (StringUtils.isEmpty(reserveField)) {
            return false;
        }
        JSONObject obj = JSONObject.parseObject(reserveField);
        return ObjectUtil.isNotEmpty(obj.getInteger("earlyScreenPass")) && obj.getInteger("earlyScreenPass") == 0;
    }

    @Override
    public String label() {
        return "WuBa_TransferData_To_Customer_EspFilter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
