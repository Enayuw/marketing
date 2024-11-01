package com.br.marketing.api.customer.black.handler;


import com.br.marketing.speedconfig.MarketingCommonConfig;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * 获取客户定制上传适配策略
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
@Component
@Slf4j
public class CustomerBlackDataHandleSingleton implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private volatile static ConcurrentSkipListMap<CustomerBlackHandlerEnum, CustomerBlackDataHandler> customDataHandlerMap;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化缓存处理业务
     */
    private void cacheCustomerDataHandleImpl() {
        if (customDataHandlerMap == null) {
            synchronized (CustomerBlackDataHandleSingleton.class) {
                if (customDataHandlerMap == null) {
                    Map<String, CustomerBlackDataHandler> customDataHandleNameMap = applicationContext.getBeansOfType(
                            CustomerBlackDataHandler.class);
                    customDataHandlerMap = customDataHandleNameMap.values().stream().sorted(
                            Comparator.comparing(CustomerBlackDataHandler::customer)).collect(Collectors.toConcurrentMap(
                        CustomerBlackDataHandler::customer, Function.identity(), BinaryOperator.maxBy(
                                    Comparator.comparing(CustomerBlackDataHandler::customer)), ConcurrentSkipListMap::new));
                }
            }
        }
        try {
            Map<String, List<String>> customerHandlerEnumConfigMap = marketingCommonConfig.getCustomerBlackHandlerEnumConfigMap();
            if (customerHandlerEnumConfigMap == null) {
                return;
            }
            customDataHandlerMap.keySet().forEach(k -> {
                String key = k.toString();
                if (customerHandlerEnumConfigMap.containsKey(key)) {
                    List<String> apiCodes = customerHandlerEnumConfigMap.get(key);
                    if (apiCodes != null) {
                        k.setApiCodes(apiCodes.toArray(new String[0]));
                    }
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 根据apiCode获取客户处理,未找到时,可设置默认客户
     */
    public CustomerBlackDataHandler getCustomerDataHandleImpl(String apiCode, CustomerBlackHandlerEnum defaultCustom) {
        cacheCustomerDataHandleImpl();
        return customDataHandlerMap.get(CustomerBlackHandlerEnum.valueOf(apiCode, defaultCustom));
    }

    /**
     * 2023-10-18 20:06 根据枚举获取客户处理
     */
    public CustomerBlackDataHandler getCustomerDataHandleImpl(CustomerBlackHandlerEnum customerUploadHandlerEnum) {
        cacheCustomerDataHandleImpl();
        return customDataHandlerMap.get(customerUploadHandlerEnum);
    }

}
