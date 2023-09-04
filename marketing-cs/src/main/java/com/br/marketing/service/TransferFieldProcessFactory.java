package com.br.marketing.service;

import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 转化数据字段加工工厂
 */
public interface TransferFieldProcessFactory {
    /**
     * 客户名称
     * @return
     */
    String customerName();

    /**
     * 字段加工处理
     */
    void fieldProcess(MarketingTransferSyncUser transferSyncUser, TransferDataItemDTO transferDataItemDTO);

    TransferDataDTO formatTransferObj(String jsonData);

    default Boolean isFormat(){
        return false;
    }
}
