package com.br.marketing.check.job.decision;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.check.beanhadler.InterfaceDecisionFactory;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动化推送决策作业
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-12 11:14
 */
@Component
@Slf4j
public class AutomatedPushDecisionJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private MarketingCommonConfig commonConfig;

    @Resource
    private InterfaceDecisionFactory interfaceDecisionFactory;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start1 = System.currentTimeMillis();
        String jobParameter = context.getJobParameter();
        Map<String, JSONArray> jobPushDecisionParameterMap = commonConfig.getJobPushDecisionParameterMap();
        if (CollectionUtils.isEmpty(jobPushDecisionParameterMap)) {
            return;
        }
        Set<String> apiCodeSet = new HashSet<>();
        Set<Map.Entry<String, JSONArray>> parameterEntries = jobPushDecisionParameterMap.entrySet();
        Map<CustomerPushDecisionActionEnum, List<JobPushDecisionParameterBO>> map = new LinkedHashMap<>();
        for (Map.Entry<String, JSONArray> parameterEntry : parameterEntries) {
            CustomerPushDecisionActionEnum customerPushDecisionActionEnum = CustomerPushDecisionActionEnum.valueOf(parameterEntry.getKey().toString());
            List<JobPushDecisionParameterBO> list = JSONArray.parseArray(JSONArray.toJSONString(parameterEntry.getValue()), JobPushDecisionParameterBO.class);
            map.put(customerPushDecisionActionEnum, list);
            apiCodeSet.addAll(list.stream().map(JobPushDecisionParameterBO::getApiCode).collect(Collectors.toSet()));
        }
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"))
                .andApiCodeIn(new ArrayList<>(apiCodeSet));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExampleAndShard(
                customerExample, context.getShardingTotalCount(), context.getShardingItems());
        Set<String> customerApiCodeSet = marketingCustomers.stream().map(MarketingCustomer::getApiCode)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(customerApiCodeSet)) {
            return;
        }
        final Set<Map.Entry<CustomerPushDecisionActionEnum, List<JobPushDecisionParameterBO>>> entries = map.entrySet();
        Map<CustomerPushDecisionActionEnum, AutomatedPushDecisionService> automatedPushDecisionServiceImpl =
                interfaceDecisionFactory.getAutomatedPushDecisionServiceImpl();
        for (Map.Entry<CustomerPushDecisionActionEnum, List<JobPushDecisionParameterBO>> parameterEntry : entries) {
            CustomerPushDecisionActionEnum key = parameterEntry.getKey();
            AutomatedPushDecisionService automatedPushDecisionService = automatedPushDecisionServiceImpl.get(key);
            for (JobPushDecisionParameterBO parameter : parameterEntry.getValue()) {
                if (customerApiCodeSet.contains(parameter.getApiCode())) {
                    long start = System.currentTimeMillis();
                    List<TransferActionFront> actionFrontRows = automatedPushDecisionService.createActionFrontRows(
                            parameter, transferActionFrontMapper, jobParameter);
                    for (TransferActionFront row : actionFrontRows) {
                        int i = automatedPushDecisionService.saveActionFront(transferActionFrontMapper, row);
                        if (i < 1) {
                            log.error("客户[{}]自动化转决策任务记录保存入库失败!配置信息为:{};调度任务参数:{}", key, parameter, jobParameter);
                        }
                        TransferActionFront updateActionFront = automatedPushDecisionService.actionData(
                                row, parameter, jobParameter, methodRetryHandlerService);
                        if (updateActionFront == null) {
                            log.error("客户[{}]自动化转决策未全部成功!配置信息为:{};调度任务参数:{}", key, parameter, jobParameter);
                        } else {
                            int u = automatedPushDecisionService.updateActionFrontStatus(transferActionFrontMapper
                                    , updateActionFront);
                            if (u < 1) {
                                log.error("客户[{}]自动化转决策任务记录更新入库失败!配置信息为:{};调度任务参数:{}", key, parameter, jobParameter);
                            }
                        }

                    }
                    long end = System.currentTimeMillis();
                    log.warn("【{}】调度结束，耗时:{},配置信息为:{};调度任务参数:{}", key, end - start, parameter, jobParameter);
                }
            }
        }
        long end1 = System.currentTimeMillis();
        log.warn("【自动化推送决策】调度结束，耗时:{}", end1 - start1);
    }
}
