package com.br.marketing.check.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTransferSyncUserCell;

import java.util.List;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/15 17:57
 */
public interface JuZiCheckToDassService {
    Result transferDataPeriodToDass(String status ,List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellList);
}
