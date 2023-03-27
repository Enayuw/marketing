package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;

import java.time.LocalDate;
import java.util.Date;

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
    MarketingSyncUser getNewValidityPeriodData(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate);

    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，返回带电话的转化数据不在返回可空
     */
    MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate);

    /**
     *  判断转化数据是否在有效期内，在的话返回true，不在返回false
     */
    boolean isValidityPeriod(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate);

    /**
     * 根据apiCode和日期
     * 获取T+N规则的有效开始时间
     * @param apiCode
     * @return
     */
    Result<Date> getValidityBeginOfTn(String apiCode, Date endDate);


}
