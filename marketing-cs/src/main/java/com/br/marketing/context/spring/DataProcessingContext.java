package com.br.marketing.context.spring;

import com.br.marketing.service.Impl.dataProcess.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description DataProcessingContext
 * @Author hong.chen
 * @CreateTime 2023/11/13
 */
@Slf4j
@Component
public class DataProcessingContext implements ApplicationContextAware {
    @Getter
    private static ApplicationContext ac;

    private static final Map<String, Class<? extends DataProcessAbstractProxy>> DATA_PROXY = new HashMap<>();

    static {
        DATA_PROXY.put("ZhongBangSyncCaiFuProxy", ZhongBangSyncCaiFuProxy.class);
        DATA_PROXY.put("ZhongBangSyncXinDaiProxy", ZhongBangSyncXinDaiProxy.class);
        DATA_PROXY.put("ZhongBangTransferProxy", ZhongBangTransferProxy.class);
        DATA_PROXY.put("YiLianTransferCleanProxy", YiLianTransferCleanProxy.class);
        DATA_PROXY.put("ZhongBangAISyncCleanProxy", ZhongBangAISyncCleanProxy.class);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

    public static DataProcessAbstractProxy getBean(String dataProxy) {
        try {
            Class<? extends DataProcessAbstractProxy> proxyClass = DATA_PROXY.get(dataProxy);
            return ac.getBean(proxyClass);
        } catch (BeansException e) {
            log.error("数据处理任务 not fund dataProxy:{} --", dataProxy, e);
            throw e;
        }
    }
}
