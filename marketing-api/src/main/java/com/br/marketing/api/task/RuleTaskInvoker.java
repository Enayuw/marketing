package com.br.marketing.api.task;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.broker.StrategyApiContextHolder;
import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entities.client.HxResult;
import com.br.marketing.api.entities.client.RuleResult;
import com.br.marketing.api.entities.client.RuleTaskResult;
import com.br.marketing.api.entities.client.SanxiangzhiliResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** 规则相关任务执行器
 * 该执行器中
 * @author Wang Weiwei
 * @since 2018/3/16
 */
@Slf4j
public class RuleTaskInvoker implements StrategyApiContextHolder,LoanTaskInvoker<RuleTaskResult> {
    private StrategyApiContext strategyApiContext;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private Future<HxResult> hxResultFuture;
    private StrategyResult strategyResult;
    Future<List<SanxiangzhiliResult>> sanxiangzhiliResultFuture;

    /**
     * 规则相关任务执行器
     * @param strategyResult 策略结果
     * @param hxResultFuture 画像结果
     * @param sanxiangzhiliResultFuture 三相结果
     */
    public RuleTaskInvoker( StrategyResult strategyResult, Future<HxResult> hxResultFuture,
                            Future<List<SanxiangzhiliResult>> sanxiangzhiliResultFuture) {
        this.hxResultFuture = hxResultFuture;
        strategyResult.addErrorFlag();
        this.strategyResult = strategyResult;
        this.sanxiangzhiliResultFuture=sanxiangzhiliResultFuture;
    }


    @Override
    public StrategyApiContext getStrategyApiContext() {
        return strategyApiContext;
    }

    @Override
    public void setStrategyApiContext(StrategyApiContext strategyApiContext) {
        this.strategyApiContext = strategyApiContext;
        threadPoolTaskExecutor = strategyApiContext.getSpringContext().getBean("StrategyThreadPool", ThreadPoolTaskExecutor.class);
    }

    /**
     * 规则任务执行流程
     * 1. 执行取画像数据子流程
     * 2. 执行规则引擎子流程
     * 3. 执行规则和画像结果入库，初次建立策略api持久化任务信息（注意：  在这一步之前的任务均不能被查询接口查到）
     * 4. 执行规则逻辑子流程
     * 5. 执行风险分级子流程
     * 6. 执行最终结果入库子流程,此时修改接口状态未任务完成状态
     *
     * 如在任务执行过程中发生异常，则执行将策略记录修改为失败的状态
     * */
    @Override
    public Future<RuleTaskResult> invoker() {
        return threadPoolTaskExecutor.submit(new Callable<RuleTaskResult>() {
            @Override
            public RuleTaskResult call() throws Exception {
                try {
                    RuleTaskResult ruleTaskResult = new RuleTaskResult(new JSONObject());
                    List<SanxiangzhiliResult> futures=null;
                    if(sanxiangzhiliResultFuture!=null){
                        try {
                        futures = sanxiangzhiliResultFuture.get(6, TimeUnit.SECONDS);
                        }catch (Exception e){
                            log.error("三相之力结果异常,swiftNumber--{}",strategyApiContext.getSwiftNumber(),e);
                        }
                    }
                    HxResult hxResult=null;
                    if(hxResultFuture!=null){
                        try{
                            hxResult= hxResultFuture.get();
                        }catch (Exception e){
                            log.error("画像结果异常,swiftNumber--{}",strategyApiContext.getSwiftNumber(),e);
                        }
                    }
                    //执行画像
                    Future<RuleResult> ruleResultFuture = new RuleEngineTaskInvoker(hxResult, futures,strategyApiContext).invoker();
                    new RuleAndHxResulComposeInvoker(strategyApiContext,strategyResult, ruleResultFuture).invoker();
                    new RiskRankInvoker(strategyApiContext, strategyResult).invoker();
                    Future<StrategyResult> strategyResultFuture = new RuleLogicInvoker(strategyApiContext, strategyResult).invoker();
                    ruleTaskResult.setRuleResult(ruleResultFuture.get());
                    ruleTaskResult.setStrategyResult(strategyResultFuture.get());
                    return ruleTaskResult;
                }catch (Exception e){
                    log.error("执行规则子任务异常 --{} swiftNumber--{}",e,strategyApiContext.getSwiftNumber());
                    return null;
                }
            }
        });
    }

}
