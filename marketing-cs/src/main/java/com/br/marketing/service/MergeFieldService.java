package com.br.marketing.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * Merge Field interface
 * @Date 2024/12/5 21:31
 */
public interface MergeFieldService {

    void mergeUploadAndTransfer(JSONObject json, MarketingTransferSyncUser transfer, MarketingSyncUser syncUser);

    void formatSSSTransfer(MarketingTransferSyncUser transfer);
}
