package com.br.marketing.api.broker;

import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entities.client.*;
import com.br.marketing.api.task.CombineAndUpdateInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Future;

/**
 * The type Base loan broker.
 */
@Slf4j
public abstract class BaseLoanBroker implements  StrategyApiContextHolder{
    /**
     * The Strategy api context.
     */
    protected StrategyApiContext strategyApiContext;
    /**
     * The Api user param.
     */
    protected ApiUserParam apiUserParam;
    /**
     * The Strategy result.
     */
    protected StrategyResult strategyResult;
    /**
     * The Result.
     */
    public Result result;
    /**
     * The Rule result.
     */
    Future<RuleTaskResult> ruleResult;
    /**
     * The Hx result future.
     */
    Future<HxResult> hxResultFuture;
    /**
     * The Sanxiangzhili result future.
     */
    Future<List<SanxiangzhiliResult>> sanxiangzhiliResultFuture;
    /**
     * The Loan retrial result.
     */
    Future<LoanRetrialResult> loanRetrialResult = null;
    /**
     * The Behavior score result.
     */
    Future<BehaviorScoreResult> behaviorScoreResult = null;

    /**
     * Instantiates a new Base loan broker.
     *
     * @param apiUserParam   the api user param
     * @param strategyResult the strategy result
     * @param result         the result
     */
    public BaseLoanBroker(ApiUserParam apiUserParam, StrategyResult strategyResult, Result result) {
        this.apiUserParam = apiUserParam;
        this.strategyResult = strategyResult;
        this.result=result;
    }

    @Override
    public StrategyApiContext getStrategyApiContext() {
        return strategyApiContext;
    }

    @Override
    public void setStrategyApiContext(StrategyApiContext strategyApiContext) {
        this.strategyApiContext = strategyApiContext;
    }


    /**
     * Generate task.
     *
     * @throws Exception the exception
     */
    public void generateTask() throws Exception {
        generateBusinessTask();
        try {
            new CombineAndUpdateInvoker(strategyApiContext, strategyResult, ruleResult, hxResultFuture,
                    loanRetrialResult, behaviorScoreResult,sanxiangzhiliResultFuture).invoker();
        }catch (Exception e){
            log.error("合并任务失败:{} swiftNumber:{}",e,strategyApiContext.getSwiftNumber());
            new CombineAndUpdateInvoker(strategyApiContext, strategyResult, ruleResult, hxResultFuture,
                    loanRetrialResult, behaviorScoreResult,sanxiangzhiliResultFuture).invoker();
        }
    }

    /**
     * Generate business task.
     */
    protected abstract void generateBusinessTask();
}
