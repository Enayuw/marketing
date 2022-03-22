package com.br.marketing.context.impl;

import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description :
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/22 13:51
 */
@Service
public class HaiErRuleCollectDataImpl implements AbstractRuleCollectDataService {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        HaiErRuleNecessaryData haiErRuleNecessaryData = new HaiErRuleNecessaryData();
        List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
        Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(context.getApiCode(), set);
        Map<String, MarketingSyncUser> collect = preUserByTask.stream().collect(
                Collectors.groupingBy(MarketingSyncUser::getCustNum
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
        haiErRuleNecessaryData.setCustomerMap(collect);
        context.setRuleNecessaryData(haiErRuleNecessaryData);
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.HAI_ER_RULE_DATA_COLLECTION;
    }


    @Data
    public class HaiErRuleNecessaryData extends RuleNecessaryData {
        /**
         * 海尔客服转化所需信息
         */
        private Map<String, MarketingSyncUser> customerMap;
    }
}
