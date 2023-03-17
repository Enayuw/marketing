package com.br.marketing.service;

import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 14:44
 */
public interface TransferDataValidityPeriodService {

    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，不在返回可空
     */
    MarketingSyncUser getNewValidityPeriodData(MarketingTransferSyncUser marketingTransferSyncUser);

    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，不在返回可空
     */
    MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser);

    /**
     *  判断转化数据是否在有效期内，在的话返回true，不在返回false
     */
    boolean isValidityPeriod(MarketingTransferSyncUser marketingTransferSyncUser);

}
