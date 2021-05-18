package com.br.marketing.api.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.broker.RuleProductorsExpression;
import com.br.marketing.api.client.RedisService;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entities.client.HxResult;
import com.br.marketing.api.entities.client.RuleResult;
import com.br.marketing.api.entities.client.SanxiangzhiliResult;
import com.br.marketing.common.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

/** 规则与画像结果合并任务
 * 该执行器主要有两个功能
 * 1. 画像结果和规则引擎的结果的flag存入strategyResult的flag
 * 2. 将规则引擎的结果进行适当封装成初步满足strategyResult格式的报文(不包括风险分级与规则逻辑)
 * 3. 将策略定义的相关信息封装到 strategyResult 中
 * @author Wang Weiwei
 * @since 2018/3/17
 */
@Slf4j
public class RuleAndHxResulComposeInvoker implements LoanTaskInvoker<StrategyResult>, RuleProductorsExpression {
    /**
     * 策略返回对象中的规则列表
     * */
    private JSONArray ruleTypeArray = new JSONArray();
    private StrategyResult strategyResult;
    private Future<RuleResult> ruleResultFuture;
    private StrategyApiContext strategyApiContext;
    private JSONObject flag;

    /**
     * 构造函数
     * @param strategyApiContext strategyApiContext上下文
     * @param strategyResult 策略结果
     * @param ruleResultFuture 规则结果
     */
    public RuleAndHxResulComposeInvoker(StrategyApiContext strategyApiContext, StrategyResult strategyResult, Future<RuleResult> ruleResultFuture) {
        this.strategyResult = strategyResult;
        flag = strategyResult.getFlag();
        this.ruleResultFuture = ruleResultFuture;
        this.strategyApiContext = strategyApiContext;
        parseRuleProductors(strategyApiContext.getStrategy().getRuleType());
        convertRuleResult2Strategy();
        cleanRuleType();
    }


    /**
     * 清洗贷中命中的规则信息，将某规则集下未命中任何规则的规则集清洗掉
     * */
    private void cleanRuleType() {
        Iterator<Object> iterator = ruleTypeArray.iterator();
        while (iterator.hasNext()){
            JSONObject ruleType = (JSONObject) iterator.next();
            if (!ruleType.containsKey("ruleWeight")){
                iterator.remove();
            }else {
                flag.put(ruleType.getString("ruleType"), "1");
            }
        }
        strategyResult.setRuleArray(ruleTypeArray);
    }


    /**
     * 将规则引擎返回值转化为策略规则返回的格式
     * */
    private void convertRuleResult2Strategy() {
        try {
            JSONArray ruleList = ruleResultFuture.get().getRuleList();
            for (int i = 0; i < ruleList.size(); i++) {
                JSONObject rule = ruleList.getJSONObject(i);
                for (int j = 0; j < ruleTypeArray.size(); j++) {
                    JSONObject ruleType = ruleTypeArray.getJSONObject(j);
                    if (rule.getString("rule_type").equals(ruleType.getString("ruleType"))){
                        // 原贷中策略结果已经包含最终风险
                        updateRuleWeight(rule, ruleType);
                        add2LoanRule(ruleType, initLoanRule(rule));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取规则引擎结果异常--{}  swiftNumber--{}",e,strategyApiContext.getSwiftNumber());
        }
    }


    /**
     * 更新贷中策略规则结果中每个规则集的权重信息
     * 更新权重的策略是： 取该规则集下最大权重的规则作为该规则集的最终权重
     * */
    private void updateRuleWeight(JSONObject rule, JSONObject ruleType) {
        if (ruleType.containsKey("ruleWeight")){
            if (rule.getInteger("weight") > ruleType.getInteger("ruleWeight")){
                ruleType.put("ruleWeight", rule.getInteger("weight"));
            }
        }else {
            ruleType.put("ruleWeight", rule.getInteger("weight"));
        }
    }


    /**
     * 将转换后的贷中规则对象，加入到贷中规则数组中
     * */
    private void add2LoanRule(JSONObject ruleType, JSONObject loanRule) {
        // 当该规则集下已有贷中规则结果时
        if (ruleType.containsKey("loanRule")){
            JSONArray ruleArray = ruleType.getJSONArray("loanRule");
            ruleArray.add(loanRule);
        }else {
            JSONArray ruleArray = new JSONArray();
            ruleArray.add(loanRule);
            ruleType.put("loanRule", ruleArray);
        }
    }


    /**
     * 将规则引擎规则返回信息转化成策略贷中结果的数据结构  只转化具体的规则
     * */
    private JSONObject initLoanRule(JSONObject rule) {
        JSONObject loanRule = new JSONObject();
        loanRule.put("ruleCode", rule.getString("rule_code"));
        loanRule.put("ruleName", rule.getString("rule_name"));
        // 该key用于后续规则逻辑计算，最后返回用户时会被移除
        loanRule.put("ruleKeys", rule.getJSONArray("ruleKeys"));
        loanRule.put("weight", rule.getIntValue("weight"));
        return loanRule;
    }



    @Override
    public Future<StrategyResult> invoker() {
        return null;
    }

    @Override
    public final void parseRuleProductors(String ruleType) {
        // 等待画像请求完成，否则让步执行
        while (!ruleResultFuture.isDone()){
            log.info("ruleResultFuture.isDone:{}",ruleResultFuture.isDone());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // 策略定义中的规则列表
        JSONArray ruleArray = JSONArray.parseArray(ruleType);

        for (int i = 0; i < ruleArray.size(); i++) {
            // 模拟策略返回规则结果
            JSONObject ruleTypeResult = new JSONObject();
            JSONObject object = ruleArray.getJSONObject(i);
            ruleTypeResult.put("ruleType", object.getString("ruleType"));
            ruleTypeResult.put("version", object.getString("version"));
            ruleTypeResult.put("rule_name",object.getString("rule_name"));
            ruleTypeArray.add(ruleTypeResult);
            log.info("更新flag标识，默认为 0--{}",object.getString("ruleType"));
            // 更新flag标识，默认为 0 ，未命中任何规则
            flag.put(object.getString("ruleType"), "0");
        }
    }
}
