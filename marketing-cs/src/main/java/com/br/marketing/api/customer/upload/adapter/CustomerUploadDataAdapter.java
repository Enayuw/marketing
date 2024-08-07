package com.br.marketing.api.customer.upload.adapter;

import org.springframework.stereotype.Component;

import com.br.marketing.dto.MarketingPreUserDTO;

/**
 * 定制化客户适配器
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-23 16:28
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
    public MarketingPreUserDTO adapteeCustomerUploadData(UploadDataAdaptee adaptee) {
        return adaptee == null ? null : adaptee.adapteeRequest(adaptee.getApiCode(), new MarketingPreUserDTO());
    }

}
