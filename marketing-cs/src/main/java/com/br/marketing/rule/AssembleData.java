package com.br.marketing.rule;

import com.br.marketing.entity.MarketingTransferSyncUser;

public interface AssembleData<T extends InterfaceParams> {

    T assemble(MarketingTransferSyncUser transferSyncUser);

    boolean isNeedAssemble(MarketingTransferSyncUser transferSyncUser);

    String label();

    Integer dataDirection();
}
