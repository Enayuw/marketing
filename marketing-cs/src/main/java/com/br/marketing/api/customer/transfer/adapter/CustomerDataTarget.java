package com.br.marketing.api.customer.transfer.adapter;

import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;

/**
 * 适配
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-23 16:27
 */
public interface CustomerDataTarget {

    /**
     * 2023-10-23 16:46
     * 适配转化标准接口
     */
    TransferDataDTO<TransferDataItemDTO> transferDataRequest(TransferDataAdaptee adaptee);
}
