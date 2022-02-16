package com.br.marketing.adapter;

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

    abstract MarketingTransferSyncUser transferSyncUserRequest();

    protected final MarketingTransferSyncUser newTransferSyncUser() {
        return new MarketingTransferSyncUser();
    }
}
