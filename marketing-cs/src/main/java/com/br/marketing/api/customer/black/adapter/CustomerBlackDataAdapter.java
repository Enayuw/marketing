package com.br.marketing.api.customer.black.adapter;

import org.springframework.stereotype.Component;

/**
 * 客户黑名单数据适配器
 *
 * @author senyang.zheng
 * @date 2024/10/30
 */
@Component
public class CustomerBlackDataAdapter implements CustomerBlackDataTarget{

    /**
     * 适配客户黑名单数据
     *
     * @param adaptee 适应者
     * @return {@link T }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    @Override
    public <T> T adapteeCustomerBlackData(BaseBlackDataAdaptee<T> adaptee) {
        return adaptee == null ? null : adaptee.adapteeRequest(adaptee.getApiCode(),adaptee.getJsonData());
    }
}
