package com.br.marketing.api.broker;

import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entities.client.HxResult;
import com.br.marketing.api.task.DtbUpdateInvoker;
import com.br.marketing.api.task.HxTaskInvoker;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;

/**
 * The type Loan dtb broker.
 */
@Slf4j
public class LoanDtbBroker implements  StrategyApiContextHolder{
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
     * Instantiates a new Loan dtb broker.
     *
     * @param apiUserParam   the api user param
     * @param strategyResult the strategy result
     */
    public LoanDtbBroker(ApiUserParam apiUserParam, StrategyResult strategyResult) {
        this.apiUserParam = apiUserParam;
        this.strategyResult = strategyResult;
    }

    /**
     * Generate task.
     *
     * @throws Exception the exception
     */
    public void generateTask() throws Exception {
        Future<HxResult> hxResultFuture = null;

        long begin1 = System.currentTimeMillis();
            try {
                log.info("开始查询画像任务");
                hxResultFuture = new HxTaskInvoker(apiUserParam, strategyApiContext).invoker();
                long costTime= System.currentTimeMillis() - begin1;
                log.info("查询画像任务耗时--{}",costTime );
            }catch (Exception e){
                log.error("查询画像失败:{}  swiftNumber:{}",e,strategyApiContext.getSwiftNumber());
            }

        try {
            long   begin2 = System.currentTimeMillis();
            log.info("开始执行更新任务");
            new DtbUpdateInvoker(hxResultFuture, strategyApiContext, strategyResult).invoker();
            long costTime= System.currentTimeMillis() - begin2;
            log.info("更新任务耗时--{}", costTime);
        }catch (Exception e){
            log.error("更新任务失败",e);
        }
    }

    @Override
    public StrategyApiContext getStrategyApiContext() {
        return strategyApiContext;
    }

    @Override
    public void setStrategyApiContext(StrategyApiContext strategyApiContext) {
        this.strategyApiContext = strategyApiContext;
    }
}
