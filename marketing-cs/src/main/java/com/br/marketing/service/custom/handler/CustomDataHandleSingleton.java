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
 * 客户业务接口
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
     * 根据apiCode获取客户处理
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

    /**
     * 2023-10-18 20:06
     * 根据枚举获取客户处理
     */
    public CustomDataHandler getCustomDataHandleImpl(CustomCodeEnum customCodeEnum) {
        if (customDataHandlerMap == null) {
            getCustomDataHandleImpl(customCodeEnum.getApiCodes()[0]);
        }
        return customDataHandlerMap.get(customCodeEnum);
    }

}
