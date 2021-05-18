package com.br.marketing.api.bussinesses;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.broker.BaseLoanBroker;
import com.br.marketing.api.broker.LoanDtbBroker;
import com.br.marketing.api.broker.LoanTaskBroker;
import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 异步策略数据中转工具
 * @author Wang Weiwei
 * @since 2018/4/2
 */
@Service
@Slf4j
public class AsyncStrategyDto {
    /**
     * Invoker task.
     *
     * @param strategyApiContext the strategy api context
     * @param apiUserParam       the api user param
     * @param strategyResult     the strategy result
     * @param result             the result
     */
    public void invokerTask(StrategyApiContext strategyApiContext, ApiUserParam apiUserParam, StrategyResult strategyResult, Result result) {
        invoker(strategyApiContext, apiUserParam, strategyResult,result);
    }

    /**
     * Invoker dtb task.
     *
     * @param strategyApiContext the strategy api context
     * @param apiUserParam       the api user param
     * @param strategyResult     the strategy result
     */
    public void invokerDtbTask(StrategyApiContext strategyApiContext, ApiUserParam apiUserParam, StrategyResult strategyResult){
        try{
            LoanDtbBroker broker = new LoanDtbBroker(apiUserParam,strategyResult);
            broker.setStrategyApiContext(strategyApiContext);
            broker.generateTask();
        }catch (Exception e){
            log.error("异步执行子任务失败---{},任务信息:---{}",e,JSONObject.toJSONString(strategyApiContext));
        }
    }

    /**
     * Invoker batch task async.
     *
     * @param strategyApiContext the strategy api context
     * @param apiUserParam       the api user param
     * @param strategyResult     the strategy result
     * @param result             the result
     */
    public void invokerBatchTaskAsync(StrategyApiContext strategyApiContext, ApiUserParam apiUserParam, StrategyResult strategyResult,Result result) {
        invoker(strategyApiContext, apiUserParam, strategyResult,result);
    }

    private void invoker(StrategyApiContext strategyApiContext, ApiUserParam apiUserParam, StrategyResult strategyResult,Result result) {
        try {
            BaseLoanBroker broker = new LoanTaskBroker(apiUserParam, strategyResult,result);
            broker.setStrategyApiContext(strategyApiContext);
            broker.generateTask();
        } catch (Exception e) {
            log.error("异步执行子任务失败---{}, 任务信息:---{}--", e, JSONObject.toJSONString(strategyApiContext));
        }
    }



}
