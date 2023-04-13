package com.br.marketing.check.beanhadler;

import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 处理对决策系统业务功能的接口帮助类
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-11 9:49
 */
@Component
public class InterfaceDecisionFactory {

    @Resource
    private SpringContextHandler springContextHandler;

    /**
     * 2023-04-12 10:29
     * 自动化转决策系统实现类
     */
    public Map<CustomerPushDecisionActionEnum, AutomatedPushDecisionService> getAutomatedPushDecisionServiceImpl() {
        Map<String, AutomatedPushDecisionService> pushDecisionActionMap = springContextHandler.getBeansOfType(
                AutomatedPushDecisionService.class);
        Objects.requireNonNull(pushDecisionActionMap);
        return pushDecisionActionMap.values().stream().sorted(Comparator.comparing(
                AutomatedPushDecisionService::customerAction)).collect(Collectors.toMap(
                AutomatedPushDecisionService::customerAction, Function.identity(), BinaryOperator.maxBy(
                        Comparator.comparing(AutomatedPushDecisionService::customerAction)), LinkedHashMap::new));
    }

}
