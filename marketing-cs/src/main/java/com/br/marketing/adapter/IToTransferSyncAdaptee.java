package com.br.marketing.adapter;

import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 适配者
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/14 17:45
 */
public interface IToTransferSyncAdaptee {

    void adapteeRequest(MarketingTransferSyncUser transferSyncUser);
}
