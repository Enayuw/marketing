package com.br.marketing.adapter.transfer;

import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 目标抽象类
 * <p>
 * 客户转化数据
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/14 18:09
 */
public abstract class TransferSyncTarget {

    /**
     * 适配为转化数据
     */
    abstract MarketingTransferSyncUser transferSyncUserRequest(String taskId);

    abstract MarketingTransferSyncUser transferSyncUserRequest(String taskId, ShuheTransferJsonDTO jsonDTO);

    protected final MarketingTransferSyncUser newTransferSyncUser() {
        return new MarketingTransferSyncUser();
    }
}
