package com.br.marketing.api.customer.upload.adapter;

import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;

/**
 * 适配
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-23 16:27
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
    MarketingPreUserDTO adapteeCustomerUploadData(UploadDataAdaptee adaptee);
}
