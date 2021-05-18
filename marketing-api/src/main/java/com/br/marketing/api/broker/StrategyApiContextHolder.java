package com.br.marketing.api.broker;


import com.br.marketing.api.entities.api.StrategyApiContext;

/** 策略上下文操作接口
 * @author Wang Weiwei
 * @since 2018/3/15
 */
public interface StrategyApiContextHolder {
    /**
     * 获取策略上下文
     * @return 策略上下文
     */
    StrategyApiContext getStrategyApiContext();

    /**
     * 设置策略上下文
     * @param strategyApiContext 策略上下文
     */
    void setStrategyApiContext(StrategyApiContext strategyApiContext);
}
