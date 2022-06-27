package com.br.marketing.context.impl;

import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
 * @Description : 拍拍贷收集上下文
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/31 17:03
 */

@Service
public class PPDCollectDataImpl extends CommonMethodHandlerService{

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;


    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            PPDRuleNecessaryData ruleNecessaryData = new PPDRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            String cId = tableCreateService.getTcId(context.getApiCode());
            Map<String, MarketingTransferSyncUser> collect = customerMarketingTransferSyncUser(set, context.getApiCode(),cId);
            ruleNecessaryData.setCustomerTransferMap(collect);
            ruleNecessaryData.setCustomerMap(customerMarketingSyncUser(set,context.getApiCode()));
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    private Map<String, MarketingTransferSyncUser> customerMarketingTransferSyncUser(Set<String> set, String apiCode,String tcId) {
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).
                andCustNumIn(new ArrayList<>(set));
        example.settCid(tcId);
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        return transferList.stream().collect(
                Collectors.groupingBy(MarketingTransferSyncUser::getCustNum
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.PPD_DATA_COLLECTION;
    }





    @Data
    public class PPDRuleNecessaryData extends RuleNecessaryData {
        /**
         * 拍拍贷转化所需信息
         */
        private Map<String, MarketingTransferSyncUser> customerTransferMap;

        /**
         * 客户上传表信息
         */
        private Map<String, MarketingSyncUser> customerMap;

    }
}
