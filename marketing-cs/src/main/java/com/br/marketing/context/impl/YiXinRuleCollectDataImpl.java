package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
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
 * @Description : 宜信实时推电销上下文处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/31 17:03
 */

@Service
public class YiXinRuleCollectDataImpl extends CommonMethodHandlerService{

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            YiXinRuleNecessaryData ruleNecessaryData = new YiXinRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> syncUser =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, context.getApiCode(), new Date());
            ruleNecessaryData.setCustomerMap(syncUser);
            Map<String,String> blackList = queryBlackData(transferList,context.getApiCode());
            ruleNecessaryData.setBlackList(blackList);

            String cId = tableCreateService.getCId(context.getApiCode());
            List<CallRecord> callRecordNewByCustNum = callRecordMapper.getLastCallRecordByCustNum(set, cId);
            Map<String, List<String>> callRecords = callRecordNewByCustNum.stream().collect(Collectors.groupingBy(CallRecord::getCaseNum
                    , Collectors.mapping(CallRecord::getIntentionGrade, Collectors.toList())));
            ruleNecessaryData.setCallRecordMap(callRecords);
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION;
    }


    private Map<String, String> queryBlackData(List<MarketingTransferSyncUser> transferList,String apiCode) {
        HashMap<String, String> map = new HashMap<>();
        /**
         * 黑名单查询接口 每1000条数据一个批次
         */
        int pageSize = 500;
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
    public class YiXinRuleNecessaryData extends RuleNecessaryData {
        /**
         * 宜信实时推电销所需信息
         */
        private Map<String, SyncUserValidityPeriodsBO> customerMap;

        /**
         * 宜信实时推电销黑名单
         */
        private Map<String,String> blackList;

        /**
         * 通话明细信息
         */
        private Map<String, List<String>> callRecordMap;
    }
}
