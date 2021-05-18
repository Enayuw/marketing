package com.br.marketing.api.validator;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entity.Strategy;
import com.br.marketing.api.entity.StrategyDetail;
import com.br.marketing.common.constants.strategy.ModuleSwitch;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.List;

/**
 * 策略校验器
 */
public class StrategyValidator implements ConstraintValidator<StategyValidatorAnnotation, Object>{
    @Override
    public void initialize(StategyValidatorAnnotation constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof Strategy){
            Strategy strategy = (Strategy) value;
            return check(strategy.getRuleType(), strategy.getStrategyRetry(), strategy.getBehaviorScore(), strategy.getSelfhealScore());
        }else if (value instanceof StrategyDetail){
            StrategyDetail strategy = (StrategyDetail)value;
            return check(strategy.getRuleType(), strategy.getStrategyRetry(), strategy.getBehaviorScore(), strategy.getSelfhealScore());
        }
        return true;
    }
    //未开通模块|未开启模块
    private boolean check(String ruleType, String strategyRetry, String behaviorScore, String selfScore) {
        return new Function<List<String>, Boolean>() {
            @Override
            public Boolean apply(List<String> list) {
                list.removeAll(Collections.singleton(null));
                return list.size() != 0 && new Function<List<String>, Boolean>() {
                    @Override
                    public Boolean apply(List<String> list) {
                        boolean flag = false;
                        for (String str : list) {
                            if (ModuleSwitch.OPEN.getCode().equals(JSONObject.parseObject(str).getInteger("status"))) {
                                flag = true;
                            }
                        }
                        return flag;
                    }
                }.apply(list);
            }
        }.apply(Lists.newArrayList(ruleType, strategyRetry, behaviorScore, selfScore));
    }
}
