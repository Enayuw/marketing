package com.br.marketing.service.rulecenter;

import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class RuleCenterBySourceTypeFactory implements ApplicationContextAware {


    Map<Integer,IRuleCenterFilterTemplateService> iRuleCenterFilterTemplateServiceHashMap = new HashMap<>();

    Map<Integer,IRuleTaskService> iRuleCenterTaskServiceHashMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IRuleCenterFilterTemplateService> ruleFilterMap = applicationContext.getBeansOfType(IRuleCenterFilterTemplateService.class);
        ruleFilterMap.entrySet().forEach(t-> iRuleCenterFilterTemplateServiceHashMap.putIfAbsent(t.getValue().sourceLabel().getCode(),t.getValue()));

        Map<String, IRuleTaskService> ruleTaskMap = applicationContext.getBeansOfType(IRuleTaskService.class);
        ruleTaskMap.entrySet().forEach(t-> iRuleCenterTaskServiceHashMap.putIfAbsent(t.getValue().sourceLabel().getCode(),t.getValue()));
    }

    public IRuleCenterFilterTemplateService getFileterTemplate(Integer sourceType){
        return iRuleCenterFilterTemplateServiceHashMap.get(sourceType);
    }

    public IRuleTaskService getRuleTaskService(Integer sourceType){
        return iRuleCenterTaskServiceHashMap.get(sourceType);
    }

}
