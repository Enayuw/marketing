package com.br.marketing.check.service;

import com.br.marketing.entity.MarketingTransferSyncUserCell;

import java.util.List;
import java.util.Set;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/15 17:57
 */

public interface OriginPeriodPredicateService {
    void  transferDataPeriod(String apiCode, String status , Set<MarketingTransferSyncUserCell> marketingTransferSyncUserCellList);


}
