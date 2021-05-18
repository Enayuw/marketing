package com.br.marketing.api.broker;

/** 规则与数据产品解释接口
 * @author Wang Weiwei
 * @since 2018/3/17
 */
public interface RuleProductorsExpression {

    /**
     * 解释需要调用的规则产品或画像产品名，生成调用参数字符串
     * @param ruleType  @see ${Strategy.ruleType}
     * */
    void parseRuleProductors(String ruleType);
}
