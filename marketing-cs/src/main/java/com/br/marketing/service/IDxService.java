package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSale;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDxService {
    /**
     * 获取电销记录的custNum
     * @return
     */
    Set<String> getCustNumByPhoneDx(Collection custNums, String _tApicode, String _startDay, String _endDay, String _transferType);

    Result<Map<String, String>> getBlackByTransfer(List<MarketingTransferSyncUser> transferSyncUsers,String apiCode);

    Result<Map<String, String>> getBlackByDXfile(List<PhoneSale> phoneSales, String apiCode);
}
