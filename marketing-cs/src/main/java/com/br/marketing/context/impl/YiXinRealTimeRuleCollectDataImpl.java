package com.br.marketing.context.impl;

import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @Description : 宜信实时推电销上下文处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/31 17:03
 */

@Service
public class YiXinRealTimeRuleCollectDataImpl extends CommonMethodHandlerService{

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            YiXinRealTimeRuleNecessaryData ruleNecessaryData = new YiXinRealTimeRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Map<String, MarketingSyncUser> collect = customerMarketingSyncUser(transferList, context.getApiCode());
            Map<String,String> blackList = queryBlackData(transferList,context.getApiCode());
            ruleNecessaryData.setCustomerMap(collect);
            ruleNecessaryData.setBlackList(blackList);
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.YI_XIN_REALTIME_DATA_COLLECTION;
    }


    private Map<String, String> queryBlackData(List<MarketingTransferSyncUser> transferList,String apiCode) {
        HashMap<String, String> map = new HashMap<>();
        /**
         * 黑名单查询接口 每1000条数据一个批次
         */
        int pageSize = 1000;
        int totalCount = transferList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<MarketingTransferSyncUser> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
            }
            List<BlackQueryDetailDTO> list = new ArrayList<>();
            for (MarketingTransferSyncUser syncUser : subList) {
                BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
                blackQueryDetailDTO.setDataId(syncUser.getId().toString());
                blackQueryDetailDTO.setApiCode(apiCode);
                blackQueryDetailDTO.setCaseNum(syncUser.getCustNum());
                list.add(blackQueryDetailDTO);
            }

            ReqBlackPhoneQueryDTO reqBlackPhoneQueryDTO = new ReqBlackPhoneQueryDTO();
            reqBlackPhoneQueryDTO.setApiCode(apiCode);
            reqBlackPhoneQueryDTO.setDetailBlackPhoneDTO(list);
            Result<Map<String,String>> result = robotaiApiServiceClient.queryBlackPhone(reqBlackPhoneQueryDTO);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())){
                map.putAll(result.getData());
            }
        }
        return map;
    }


    @Data
    public class YiXinRealTimeRuleNecessaryData extends RuleNecessaryData {
        /**
         * 宜信实时推电销所需信息
         */
        private Map<String, MarketingSyncUser> customerMap;

        /**
         * 宜信实时推电销黑名单
         */
        private Map<String,String> blackList;
    }
}
