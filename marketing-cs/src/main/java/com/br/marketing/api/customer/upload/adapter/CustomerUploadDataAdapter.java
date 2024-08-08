package com.br.marketing.api.customer.upload.adapter;

import org.springframework.stereotype.Component;

import com.br.marketing.dto.MarketingPreUserDTO;


/**
 * 客户上传数据适配器
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
@Component
public class CustomerUploadDataAdapter implements CustomerUploadDataTarget {

    /**
     * 适配客户上传数据
     *
     * @param adaptee 适配器
     * @return {@link MarketingPreUserDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public MarketingPreUserDTO adapteeCustomerUploadData(BaseUploadDataAdaptee adaptee) {
        return adaptee == null ? null : adaptee.adapteeRequest(adaptee.getApiCode(), new MarketingPreUserDTO());
    }

}
