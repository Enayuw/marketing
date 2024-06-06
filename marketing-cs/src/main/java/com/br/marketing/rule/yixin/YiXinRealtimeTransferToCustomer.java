package com.br.marketing.rule.yixin;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description : 宜信实时数据推客服
 * @Author : songjuanjuan
 * @Date : Create in 2022/3/28 10:28
 */
@Service
@Slf4j
public class YiXinRealtimeTransferToCustomer implements AssembleData<ConversionData> {

    @Autowired
    private IMarketingSyncUserService iMarketingSyncUserService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        log.warn("实时数据推客服,id={},apicode={},custNum={}",transfer.getId(),transfer.getApiCode(),transfer.getCustNum());
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setInversionStatus("0");
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setInversionDate(transfer.getTransformTime());
        conversionData.setTransformType("1");
        conversionData.setTaskId(context.getTransferInfoId().toString());
        conversionData.setEffectiveDate(transfer.getRequestTime());
        if (!StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重设置
        conversionData.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        conversionData.setSoleType(1);
        // 有效期设置 transformType=1实时数据生效截止时间需要传输T日23：59：59
        conversionData.setExpireDate(DateUtil.today() + " 23:59:59");
        // platApiCode、effectiveDate没传
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        boolean flag = Boolean.FALSE;
        if(transmitFact instanceof MarketingTransferSyncUser){
            //取指定cid下transformType为1的对应liveType值的custNum，同一custNum当天仅推送一次
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData)context.getRuleNecessaryData();
            SyncUserValidityPeriodsBO userValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
            if (userValidityPeriodsBO == null) {
                return false;
            }
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)){
                JSONObject json = JSON.parseObject(reserveField1);
                Integer transformType = json.getInteger("transformType");
                boolean isTransfromType = !StringUtils.isEmpty(transformType) && 1 == transformType;
                Integer liveType = json.getInteger("liveType");
                if(isTransfromType && (Arrays.asList(1,2,3,4,6,8).contains(liveType))){
                    //推送日+6天闭区间（例如1号推送转化数据同时推送失效时间是7号23：59：59）
                    Date requestDate = null;
                    try {
                        requestDate = DateUtils.parse(transfer.getRequestTime(), "yyyy-MM-dd HH:mm:ss");
                    } catch (ParseException e) {
                        e.printStackTrace();
                        log.warn("日期转换出错！(YiXinRealtimeTransferToCustomer),请确认id={}的RequestTime的数据格式！",transfer.getId());
                    }
                    Boolean isPeriod = iMarketingSyncUserService.isPeriodOfValidity(new Date(), 6, requestDate);
                    if (!isPeriod) {
                        log.warn("实时推客服id={}的数据不在推送日+6天闭区间内。",transfer.getId());
                        return flag;
                    }
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "YiXin_RealtimeData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
