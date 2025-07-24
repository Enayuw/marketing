package com.br.marketing.origin.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.SmsCallBackBO;
import com.br.marketing.entity.SmsCallback;
import com.br.marketing.mapper.SmsCallbackMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据来源于 客服拨打记录表
 */
@Service
@Slf4j
public class MarketingCustomerSmsCallbackDataImpl implements OriginDataService {

    @Autowired
    private SmsCallbackMapper smsCallbackMapper;

    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {

        List<Object> list = new ArrayList<>();
        // 1 根据保存到队列的ID查询记录对应的数据
        SmsCallback smsCallback = smsCallbackMapper.selectByPrimaryKey(mqFact.getSourceId());

        SmsCallBackBO bo = new SmsCallBackBO();
        BeanUtils.copyProperties(smsCallback, bo);
        list.add(bo);
        context.setApiCode(bo.getApiCode());
        context.setTransferInfoId(bo.getId());
        return list;
    }

    @Override
    public TransferSource source() {
        return TransferSource.CUSTOMER_SMS_CALLBACK;
    }

    @Override
    public List<Long> getIdList(List<Object> collect) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            SmsCallBackBO smsCallBackBO = (SmsCallBackBO)collect.get(i);
            idList.add(smsCallBackBO.getId());
        }
        return idList;
    }
}
