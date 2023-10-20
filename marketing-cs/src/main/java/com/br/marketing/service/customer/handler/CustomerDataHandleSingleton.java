package com.br.marketing.service.customer.handler;

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
public class CustomerDataHandleSingleton implements ApplicationContextAware {
    public ApplicationContext applicationContext;
    private volatile static ConcurrentSkipListMap<CustomerHandlerEnum, CustomerDataHandler> customDataHandlerMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化缓存处理业务
     */
    private void cacheCustomDataHandleImpl() {
        if (customDataHandlerMap == null) {
            synchronized (CustomerDataHandleSingleton.class) {
                if (customDataHandlerMap == null) {
                    Map<String, CustomerDataHandler> customDataHandleNameMap = applicationContext.getBeansOfType(
                            CustomerDataHandler.class);
                    customDataHandlerMap = customDataHandleNameMap.values().stream().sorted(
                            Comparator.comparing(CustomerDataHandler::custom)).collect(Collectors.toConcurrentMap(
                            CustomerDataHandler::custom, Function.identity(), BinaryOperator.maxBy(
                                    Comparator.comparing(CustomerDataHandler::custom)), ConcurrentSkipListMap::new));
                }
            }
        }
    }

    /**
     * 根据apiCode获取客户处理
     */
    public CustomerDataHandler getCustomDataHandleImpl(String apiCode) {
        cacheCustomDataHandleImpl();
        return customDataHandlerMap.get(CustomerHandlerEnum.valueof(apiCode));
    }

    /**
     * 根据apiCode获取客户处理,未找到时,可设置默认客户
     */
    public CustomerDataHandler getCustomDataHandleImpl(String apiCode, CustomerHandlerEnum defaultCustom) {
        cacheCustomDataHandleImpl();
        return customDataHandlerMap.get(CustomerHandlerEnum.valueof(apiCode, defaultCustom));
    }

    /**
     * 2023-10-18 20:06
     * 根据枚举获取客户处理
     */
    public CustomerDataHandler getCustomDataHandleImpl(CustomerHandlerEnum customerHandlerEnum) {
        cacheCustomDataHandleImpl();
        return customDataHandlerMap.get(customerHandlerEnum);
    }

}
