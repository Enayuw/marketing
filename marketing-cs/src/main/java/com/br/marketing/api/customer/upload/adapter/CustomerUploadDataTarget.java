package com.br.marketing.api.customer.upload.adapter;

import com.br.marketing.dto.MarketingPreUserDTO;

/**
 * 客户定制上传数据适配接口
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
public interface CustomerUploadDataTarget {

    /**
     * 传输数据请求
     *
     * @param adaptee 适配器
     * @return {@link MarketingPreUserDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    <T> T adapteeCustomerUploadData(BaseUploadDataAdaptee<T> adaptee);
}
