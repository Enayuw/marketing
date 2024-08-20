package com.br.marketing.api.customer.transfer.adapter;

import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import org.springframework.stereotype.Component;

/**
 * 定制化客户适配器
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-23 16:28
 */
@Component
public class CustomerDataAdapter implements CustomerDataTarget {
    @Override
    public TransferDataDTO<TransferDataItemDTO> transferDataRequest(TransferDataAdaptee adaptee) {
        return adaptee == null ? null : adaptee.adapteeRequest(adaptee.getApiCode(), new TransferDataDTO<>());
    }
}
