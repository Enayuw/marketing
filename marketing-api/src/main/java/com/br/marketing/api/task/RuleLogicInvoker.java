package com.br.marketing.api.task;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;


/**
 * 规则逻辑任务执行器
 *
 * @author Wang Weiwei
 * @since 2018/3/20
 */
@Slf4j
public class RuleLogicInvoker extends BaseRetryTaskInvoker<StrategyResult> {
    private StrategyResult strategyResult;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     *骨子额逻辑任务执行器
     * @param strategyApiContext 上下文
     * @param strategyResult 策略结果
     */
    public RuleLogicInvoker(StrategyApiContext strategyApiContext, StrategyResult strategyResult) {
        super();
        this.strategyResult = strategyResult;
        BeanFactory beanFactory = strategyApiContext.getSpringContext();
        threadPoolTaskExecutor = beanFactory.getBean("ThreadPool", ThreadPoolTaskExecutor.class);
    }

    /**
     * 远程调用规则逻辑 getBRuleDescs 接口，
     *
     * @Param 1. 经过规则结果转换后的数据格式样例
     * [
     * {
     * "ruleType":"RuleSpecialList", // 规则集编号
     * "version":"1.0",
     * "ruleWeight":80, // 规则集权重
     * "loanRule" : [
     * {
     * "ruleCode" : "QJS020",
     * "ruleName": "直系亲属银行不良",
     * "weight":80, //规则权重
     * "ruleKeys": ["1","1","1"]  // 规则变量值
     * }
     * ]
     * },{
     * "ruleType":"RuleSpecialList", // 规则集编号
     * "version":"1.0",
     * "ruleWeight":80, // 规则集权重
     * "loanRule" : [
     * {
     * "ruleCode" : "QJS020",
     * "ruleName": "直系亲属银行不良",
     * "weight":80, //规则权重
     * "ruleKeys": ["1","1","1"]  // 规则变量值
     * },{
     * "ruleCode" : "QJS020",
     * "ruleName": "直系亲属银行不良",
     * "weight":80, //规则权重
     * "ruleKeys": ["1","1","1"]  // 规则变量值
     * }
     * ]
     * }
     * ]
     * <p>
     * ////规则中心返回结果
     * {"code":"000000",
     * "data":[
     * {"ruleCode":"WJS238",
     * "logics":["||","||",""],
     * "operators":["==","==","=="],
     * "ruleType":"Rule_W_SpecialList","ruleName":"朋友或其他关系现金类分期失联",
     * "fields":["r3_id_nbank_ca_lost","r3_cell_nbank_ca_lost","r3_gid_nbank_ca_lost"],
     * "params":["2","2","2"],
     * "priority":40,
     * "fieldsZn":["通过身份证查询现金类分期高风险","通过手机号查询现金类分期高风险","通过百融用户全局标识查询现金类分期高风险"]},
     * {"ruleCode":"WJS239",
     * "logics":["||","||",""],
     * "operators":["==","==","=="],
     * "ruleType":"Rule_W_SpecialList",
     * "ruleName":"朋友或其他关系代偿类分期失联",
     * "fields":["r3_id_nbank_com_lost","r3_cell_nbank_com_lost","r3_gid_nbank_com_lost"],
     * "params":["2","2","2"],
     * "priority":40,
     * "fieldsZn":["通过身份证查询代偿类分期高风险","通过手机号查询代偿类分期高风险","通过百融用户全局标识查询代偿类分期高风险"]}
     * ],
     * "message":"成功"}
     */

    @Override
    Future<StrategyResult> retryInvoker() {
        return threadPoolTaskExecutor.submit(new Callable<StrategyResult>() {
            @Override
            public StrategyResult call() throws Exception {
                log.debug("******ruleArray的数据格式-{}-",JSONObject.toJSONString(strategyResult.getRuleArray()));
                return strategyResult;
            }
        });
    }
}
