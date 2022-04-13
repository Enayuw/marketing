package com.br.marketing.adapter.transfer;

import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 客户转化数据适配器
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/14 17:33
 */
public class TransferSyncAdapter extends TransferSyncTarget {
    private final IToTransferSyncAdaptee iToTransferSyncAdaptee;

    public TransferSyncAdapter(IToTransferSyncAdaptee iToTransferSyncAdaptee) {
        this.iToTransferSyncAdaptee = iToTransferSyncAdaptee;
    }

    @Override
    public MarketingTransferSyncUser transferSyncUserRequest(String taskId) {
        MarketingTransferSyncUser transferSyncUser = this.newTransferSyncUser();
        iToTransferSyncAdaptee.adapteeRequest(transferSyncUser, taskId);
        return transferSyncUser;
    }

    @Override
    public MarketingTransferSyncUser transferSyncUserRequest(String taskId, ShuheTransferJsonDTO jsonDTO) {
        MarketingTransferSyncUser transferSyncUser = this.newTransferSyncUser();
        iToTransferSyncAdaptee.adapteeRequest(transferSyncUser, taskId, jsonDTO);
        return transferSyncUser;
    }
}
