package com.br.marketing.rule;

import com.br.marketing.common.enums.AssembleTransferEnum;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;

public interface AssembleData<T extends InterfaceParams> {

    T assemble(MarketingTransferSyncUser transferSyncUser);

    boolean isNeedAssemble(MarketingTransferSyncUser transferSyncUser);

    default AssembleTransferEnum interfaceType(){
        return AssembleTransferEnum.DEFAULT;
    }

    String label();

    Integer dataDirection();
}
