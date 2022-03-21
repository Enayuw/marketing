package com.br.marketing.origin.impl;

import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailDTO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.TransferSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源于 客服拨打记录表
 */
@Service
@Slf4j
public class MarketingCustomerCallRecordDataImpl implements OriginDataService {

    @Autowired
    private CallRecordMapper callRecordMapper;

    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {

        List<Object> list = new ArrayList<>();
        // 1 根据保存到队列的ID查询记录对应的数据
        CallRecord callRecord = callRecordMapper.selectByPrimaryKey(mqFact.getSourceId());
        callRecord.setId(mqFact.getSourceId());

        CallRecordBO bo = new CallRecordBO();
        CallRecordDetailDTO callRecordDetailDTO = new CallRecordDetailDTO();
        BeanUtils.copyProperties(callRecord,bo);
        BeanUtils.copyProperties(callRecord,callRecordDetailDTO);
        bo.setDetail(callRecordDetailDTO);

        if(mqFact.getIsDelay()!=null && mqFact.getIsDelay()==1){
            bo.setDataSource(1);
        }else {
            bo.setDataSource(0);
        }
        log.warn("collect()拨打记录数据，id={},data={}",mqFact.getSourceId(),bo);
        list.add(bo);
        /**
         * 将查询信息放入全局上下文中
         */
        context.setApiCode(bo.getApiCode());
        context.setTransferInfoId(bo.getId());
        return list;
    }

    @Override
    public TransferSource source() {
        return TransferSource.CUSTOMER_CALL_RECORD;
    }
}
