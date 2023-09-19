package com.br.marketing.rule.xiecheng;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConvTypeConfigConversionData;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.XieChengVTRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * @Description : 携程客服转化规则 cpa
 * https://c.100credit.cn/pages/viewpage.action?pageId=125100190
 * ---------------------------------
 * @Author : 张广超
 * @Date : Create in 2023/09/19 10:28
 * cpa 3710090 转化数据推客服 判断110 且无106 的数据推
 */
@Service
@Slf4j
public class XieChengCustomerTransferCpaFromDelayImpl implements AssembleData<ConversionData> {


    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConvTypeConfigConversionData conversionData = new ConvTypeConfigConversionData();
        // 设置convType
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            String convType = json.getInteger("convType").toString();
            conversionData.setConvType(convType);
        }
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        String query = RpcClientProxy.decode(transfer.getCustNum(), "cell", "sha", "");
        conversionData.setPhone(query);
        if (!StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer  convType = json.getInteger("convType");
                if (convType == 110) {
                    XieChengVTRuleCollectDataImpl.XieChengRuleNecessaryData necessaryData =
                            (XieChengVTRuleCollectDataImpl.XieChengRuleNecessaryData) context.getRuleNecessaryData();
                    Map<String, XieChengJudgeConvTypeValue> map = necessaryData.getMap();
                    if (CollectionUtils.isEmpty(map)) {
                        return Boolean.FALSE;
                    }
                    SyncUserValidityPeriodBO periodBO = necessaryData.getValidMap().get(transfer.getCustNum());
                    if (ObjectUtil.isEmpty(periodBO)) {
                        return Boolean.FALSE;
                    }
                    Boolean hasApplySuccess = map.get(transfer.getCustNum()).getHasApplySuccess();
                    // 转化数据convType没有106  true 是有106
                    if (!hasApplySuccess) {
                        return Boolean.TRUE;
                    }
                }else {
                    return Boolean.TRUE;
                }
            }

        }
        return Boolean.FALSE;
    }

    @Override
    public String label() {
        return "XieCheng_TransferData_CPA_From_Delay_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_BY_CONVTYPE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.XIECHENG_DATA_COLLECTION_VT.getCode();
    }
}
