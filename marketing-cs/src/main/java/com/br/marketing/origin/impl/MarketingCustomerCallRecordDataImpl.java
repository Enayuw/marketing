package com.br.marketing.origin.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailBO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        CallRecordBO bo = new CallRecordBO();
        CallRecordDetailBO callRecordDetailBO = new CallRecordDetailBO();
        BeanUtils.copyProperties(callRecord, bo);
        BeanUtils.copyProperties(callRecord, callRecordDetailBO);
        bo.setDetail(callRecordDetailBO);
        try {
            Map map = (Map) JSONObject.parse(bo.getDetail().getUserProperties());
            if (StringUtils.isNotEmpty(map) && StringUtils.isNotEmpty(map.get("groupType"))) {
                String groupType = map.get("groupType").toString().trim();
                bo.setUserType(groupType);
            }
        } catch (Exception ignored) {
        }
        if (mqFact.getIsDelay() != null && mqFact.getIsDelay() == 1) {
            bo.setDataSource(1);
        } else {
            bo.setDataSource(0);
        }
//        log.warn("collect()拨打记录数据，id={}",mqFact.getSourceId());
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

    @Override
    public List<Long> getIdList(List<Object> collect) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            CallRecordBO callRecord = (CallRecordBO)collect.get(i);
            idList.add(callRecord.getId());
        }
        return idList;
    }
}
