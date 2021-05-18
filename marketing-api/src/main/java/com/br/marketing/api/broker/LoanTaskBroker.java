package com.br.marketing.api.broker;

import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.task.HxTaskInvoker;
import com.br.marketing.api.task.RuleTaskInvoker;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Loan task broker.
 */
@Slf4j
public class LoanTaskBroker extends BaseLoanBroker {
    /**
     * Instantiates a new Loan task broker.
     *
     * @param apiUserParam   the api user param
     * @param strategyResult the strategy result
     * @param result         the result
     */
    public LoanTaskBroker(ApiUserParam apiUserParam, StrategyResult strategyResult, Result result){
        super(apiUserParam, strategyResult,result);
    }

    @Override
    protected void generateBusinessTask() {
        boolean flag = false;

        //判断是否有评分或者规则集相关任务
        if( this.strategyApiContext.getHaveHx()){
            try {
                hxResultFuture = new HxTaskInvoker(apiUserParam, this.strategyApiContext).invoker();
                flag = true;
            }catch (Exception e){
                log.error("请求画像任务异常--{}  swiftNumber--{}",e,strategyApiContext.getSwiftNumber());
            }
        }


        // 判断是否有规则相关任务
        if (this.strategyApiContext.getHaveRule() && flag){
            try{
                RuleTaskInvoker ruleTaskInvoker = new RuleTaskInvoker(strategyResult,hxResultFuture,sanxiangzhiliResultFuture);
                ruleTaskInvoker.setStrategyApiContext(this.strategyApiContext);
                ruleResult = ruleTaskInvoker.invoker();
            }catch (Exception e){
                log.error("执行规则任务失败--{}  swiftNumber--{}",e,strategyApiContext.getSwiftNumber());
            }
        }
    }
}
