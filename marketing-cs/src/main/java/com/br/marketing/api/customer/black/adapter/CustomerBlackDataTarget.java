package com.br.marketing.api.customer.black.adapter;

/**
 * 客户定制黑名单数据适配接口
 *
 * @author senyang.zheng
 * @date 2024/10/30
 */
public interface CustomerBlackDataTarget {

    /**
     * 适配客户黑名单数据
     *
     * @param adaptee 适应者
     * @return {@link T }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    <T> T adapteeCustomerBlackData(BaseBlackDataAdaptee<T> adaptee);
}
