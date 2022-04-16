package com.br.marketing.context.impl;

import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
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
 * @Description : 规则收集所需通用方法
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/31 16:43
 */

@Service
public class CommonMethodHandlerService implements AbstractRuleCollectDataService {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {

    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.DEFAULT_DATA_COLLECTION;
    }

    public Map<String, MarketingSyncUser> customerMarketingSyncUser(Set<String> set, String apiCode){

        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
        return preUserByTask.stream().collect(
                Collectors.groupingBy(MarketingSyncUser::getCustNum
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
    }
}
