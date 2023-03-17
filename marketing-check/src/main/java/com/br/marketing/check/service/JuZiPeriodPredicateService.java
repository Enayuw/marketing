package com.br.marketing.check.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTransferSyncUserCell;

import java.util.List;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/15 17:57
 */
@FunctionalInterface
public interface JuZiPeriodPredicateService {
    void  transferDataPeriod(String status ,List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellList);
}
