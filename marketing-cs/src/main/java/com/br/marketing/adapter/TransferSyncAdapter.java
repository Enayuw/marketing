package com.br.marketing.adapter;

import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 客户转化数据适配器
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/14 17:33
 */
public class TransferSyncAdapter extends TransferSyncTarget {
    private IToTransferSyncAdaptee IToTransferSyncAdaptee;

    private TransferSyncAdapter() {
    }

    public TransferSyncAdapter(IToTransferSyncAdaptee IToTransferSyncAdaptee) {
        this.IToTransferSyncAdaptee = IToTransferSyncAdaptee;
    }

    @Override
    public MarketingTransferSyncUser transferSyncUserRequest(String taskId) {
        MarketingTransferSyncUser transferSyncUser = this.newTransferSyncUser();
        IToTransferSyncAdaptee.adapteeRequest(transferSyncUser, taskId);
        return transferSyncUser;
    }
}
