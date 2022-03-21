package com.br.marketing.origin.impl;

import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;

/**
 * 数据来源于 客服拨打记录表
 */
@Service
@Slf4j
public class MarketingCustomerCallRecordDataImpl implements OriginDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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


    /**
     *  获取该数据流程，数据需要匹配的规则
     * @param mqFact
     * @param context
     * @return
     */
    @Override
    public List<AssembleData> patternMatch(MqFact mqFact, ProcessHandlerContext context) {

        HashMap<String, String> customerRuleMapping = marketingCommonConfig.getCustomerRuleMapping();
        /**
         * 1、获取 apiCode获取所需的规则匹配方法
         */
        List<AssembleData> assembleDataList = new ArrayList<>();
        Collection<AssembleData> values = InterfaceHandlerFactory.assembleDataMap.values();
        CallRecord callRecord = callRecordMapper.selectByPrimaryKey(mqFact.getSourceId());
        for (AssembleData assembleData : values) {
            if (assembleData.label().startsWith(customerRuleMapping.get(callRecord.getApiCode()))){
                assembleDataList.add(assembleData);
            }
        }
        log.warn("拨打数据匹配的规则有{}个：{},{}",assembleDataList.size(),assembleDataList.get(0),assembleDataList.get(1));
        return assembleDataList;
    }
}
