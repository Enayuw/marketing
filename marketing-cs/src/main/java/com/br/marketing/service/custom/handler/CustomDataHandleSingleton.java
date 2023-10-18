package com.br.marketing.service.custom.handler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户业务接口工厂
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 9:49
 */
@Component
public class CustomDataHandleSingleton implements ApplicationContextAware {
    public ApplicationContext applicationContext;
    private volatile static ConcurrentSkipListMap<CustomCodeEnum, CustomDataHandler> customDataHandlerMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 客户处理接口集合
     */
    public CustomDataHandler getCustomDataHandleImpl(String apiCode) {
        if (customDataHandlerMap == null) {
            synchronized (CustomDataHandleSingleton.class) {
                if (customDataHandlerMap == null) {
                    Map<String, CustomDataHandler> customDataHandleNameMap = applicationContext.getBeansOfType(
                            CustomDataHandler.class);
                    customDataHandlerMap = customDataHandleNameMap.values().stream().sorted(
                            Comparator.comparing(CustomDataHandler::custom)).collect(Collectors.toConcurrentMap(
                            CustomDataHandler::custom, Function.identity(), BinaryOperator.maxBy(
                                    Comparator.comparing(CustomDataHandler::custom)), ConcurrentSkipListMap::new));
                }
            }
        }
        return customDataHandlerMap.get(CustomCodeEnum.valueof(apiCode));
    }

}
