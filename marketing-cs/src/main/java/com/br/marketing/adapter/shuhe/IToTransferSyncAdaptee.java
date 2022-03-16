package com.br.marketing.adapter.shuhe;

import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 适配者
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/14 17:45
 */
public interface IToTransferSyncAdaptee {
    /**
     * 适配
     */
    void adapteeRequest(MarketingTransferSyncUser transferSyncUser, String taskId);
}
