package com.br.marketing.rule;

import com.br.marketing.common.enums.AssembleTransferEnum;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 需要配合原始上传数据的 转化流程则实现该接口
 * @param <T>
 */
public interface AssembleDataWithSyncUser<T extends InterfaceParams> extends AssembleData {

    @Override
    default AssembleTransferEnum interfaceType() {
        return AssembleTransferEnum.WITHSYNCUSER;
    }

    boolean isNeedAssemble(MarketingTransferSyncUser transferSyncUser, MarketingSyncUser syncUser);

    T assemble(MarketingTransferSyncUser transferSyncUser, MarketingSyncUser syncUser);
}
