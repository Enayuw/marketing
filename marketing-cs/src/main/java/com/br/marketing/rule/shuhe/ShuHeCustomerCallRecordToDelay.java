package com.br.marketing.rule.shuhe;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.ProcessHandlerContext;
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

    final static String redisKeyCusNumIsFirst = "customer:callrecord:cushenwan:first:";

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        log.info("匹配上ShuHeCustomerCallRecordToDelay规则，获取的拨打记录数据id为{}",bo.getId());
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(bo.getId());
        mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
        mqFact.setIsDelay(1);
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        //正常队列消费&数据非当天首次传输-->false
        //正常队列消费&数据当天首次传输&符合规则-->推延迟队列
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof CallRecordBO){
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String key = redisKeyCusNumIsFirst+bo.getCaseNum();
            //先符合推电销的规则后,再去判断是否是当天首次传输
            Boolean pushDX = znkfPushService.isSatisfyPushDX(bo);
            Boolean isFirstToday = znkfPushService.cusNumIsFirstToday(key);
            if(StringUtils.isNotEmpty(bo.getDataSource()) && bo.getDataSource()==0 && pushDX && isFirstToday){
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "ShuHe_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MESSAGE_DELAY.getCode();
    }

}
