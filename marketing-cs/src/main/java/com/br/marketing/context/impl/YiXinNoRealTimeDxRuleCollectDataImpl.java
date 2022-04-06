package com.br.marketing.context.impl;

import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 宜信非实时推送电销数据上下文组装
 */
@Service
public class YiXinNoRealTimeDxRuleCollectDataImpl implements AbstractRuleCollectDataService {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    
    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Resource
    CallRecordMapper callRecordMapper;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            YiXinNoRealTimeDxContextRuleNecessaryData contextData = new YiXinNoRealTimeDxContextRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(context.getApiCode(), set);
            Map<String, MarketingSyncUser> collect = preUserByTask.stream().collect(
                    Collectors.groupingBy(MarketingSyncUser::getCustNum
                            , Collectors.collectingAndThen(
                                    Collectors.reducing((v1, v2) ->
                                            v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                    , Optional::get)));
            contextData.setCustomerMap(collect);
            String cId = tableCreateService.getCId(context.getApiCode());
            List<CallRecord> callRecordNewByCustNum = callRecordMapper.getLastCallRecordByCustNum(set, cId);
            Map<String, List<String>> collect1 = callRecordNewByCustNum.stream().collect(Collectors.groupingBy(CallRecord::getCaseNum
                    , Collectors.mapping(CallRecord::getIntentionGrade, Collectors.toList())));
            contextData.setCallRecordMap(collect1);
            context.setRuleNecessaryData(contextData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.YIXIN_NOREAL_DX_RULE_DATA_COLLECTION;
    }


    @Data
    public class YiXinNoRealTimeDxContextRuleNecessaryData extends RuleNecessaryData {
        /**
         * 上传客户信息
         */
        private Map<String, MarketingSyncUser> customerMap;

        /**
         * 通话明细信息
         */
        private Map<String, List<String>> callRecordMap;
    }
}
