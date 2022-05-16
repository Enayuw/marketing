package com.br.marketing.rule.shuhe;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 正常消费的数据进入延迟队列1h
 */
@Service
@Slf4j
public class ShuHeCustomerCallRecordToDelay implements AssembleData<MqFact> {

    @Autowired
    private ZnkfPushService znkfPushService;

    final static String cusNumIsFirst = "customer:callrecord:first";

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        log.warn("符合推延迟队列规则，获取的拨打记录数据id为{}", bo.getId());
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(bo.getId());
        mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
        mqFact.setIsDelay(1);
        if ("促复借".equals(bo.getUserType())) {
            mqFact.setDelayTime(0.5F);
        }
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        //正常队列消费&符合规则&数据非当天首次传输-->false
        //正常队列消费&符合规则&数据当天首次传输-->推延迟队列
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof CallRecordBO){
            CallRecordBO bo = (CallRecordBO) transmitFact;
            //先符合推电销的规则后,再去判断是否是当天首次传输
            Boolean pushDXSatisfy = znkfPushService.isSatisfyPushDX(bo);
            if(bo.getDataSource()!=0){
                return false;
            }
            if(!pushDXSatisfy){
                log.warn("callrecord数据id为{}不符合推电销规则",bo.getId());
                return false;
            }
            String key = cusNumIsFirst.concat(":").concat(bo.getUserType()).concat(":").concat(bo.getCaseNum());
            Boolean isFirstToday = znkfPushService.cusNumIsFirstToday(key);
            if(!isFirstToday){
                log.warn("callrecord数据id为{}不符合usertype={},casenum={}首次传输",bo.getId(),bo.getUserType(),bo.getCaseNum());
                return false;
            }
            if(bo.getDataSource()==0 && pushDXSatisfy && isFirstToday){
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "ShuHe_CallRecordData_MessageDelay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

}
