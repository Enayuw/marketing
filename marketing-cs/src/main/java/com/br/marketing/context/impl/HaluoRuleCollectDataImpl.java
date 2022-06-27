package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.TaskTimeMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.Data;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class HaluoRuleCollectDataImpl extends CommonMethodHandlerService {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    TaskTimeMapper taskTimeMapper;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {

            HaluoRuleNecessaryData haLuoRuleNecessaryData = new HaluoRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, MarketingSyncUser> collect = getMarketingSyncUser(set, context.getApiCode());
            haLuoRuleNecessaryData.setCustomerMap(collect);

            List<String> taskIds = collect.keySet().stream()
                    .map(t -> collect.get(t).getCusBatch()).collect(Collectors.toList());
            Map<String, List<TaskTime>> taskIdDate = getTaskIdDate(taskIds, context.getApiCode());
            haLuoRuleNecessaryData.setTaskIdDateMap(taskIdDate);

            List<String> custNums = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
            HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
            Integer taskDate = 34;
            if (haluoTransferRule != null && StringUtils.isNotBlank(haluoTransferRule.get("taskIddate"))) {
                taskDate = Integer.valueOf(haluoTransferRule.get("taskIddate")) - 1;
            }
            String endDate = LocalDate.now().format(ymd);
            String startDate = LocalDate.now().minusDays(taskDate).format(ymd);
            Map<String, List<PhoneSaleExtendHaluo>> phoneSaleExtendInfos = getPhoneSaleExtendInfos(custNums, context.getApiCode(), startDate, endDate);
            haLuoRuleNecessaryData.setPhoneSaleExtendInfoMap(phoneSaleExtendInfos);
            context.setRuleNecessaryData(haLuoRuleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.HALUO_DASS_COLLECTION;
    }


    @Data
    public class HaluoRuleNecessaryData extends RuleNecessaryData {
        /**
         * 原始上传数据
         */
        private Map<String, MarketingSyncUser> customerMap;

        /**
         * 电销记录数据
         */
        private Map<String, List<PhoneSaleExtendHaluo>> phoneSaleExtendInfoMap;

        private Map<String, List<TaskTime>> taskIdDateMap;
    }

    private Map<String, List<TaskTime>> getTaskIdDate(List<String> taskIds, String apiCode) {
        if(taskIds==null||taskIds.size()<=0){
            return new HashMap<>();
        }
        TaskTimeExample taskTimeExample = new TaskTimeExample();
        taskTimeExample.createCriteria().andTaskIdIn(taskIds)
                .andApiCodeEqualTo(apiCode);
        List<TaskTime> taskTimes = taskTimeMapper.selectByExample(taskTimeExample);
        Map<String, List<TaskTime>> collect = taskTimes.stream().collect(Collectors.groupingBy(TaskTime::getTaskId));
        return collect;
    }

    private Map<String, MarketingSyncUser> getMarketingSyncUser(Set<String> set, String apiCode){
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCustWithNoFail(apiCode, set);
        return preUserByTask.stream().collect(
                Collectors.groupingBy(MarketingSyncUser::getCustNum
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
    }
}
