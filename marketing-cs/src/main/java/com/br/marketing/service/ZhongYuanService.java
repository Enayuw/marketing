package com.br.marketing.service;

import com.br.marketing.entity.MarketingTransferSyncUser;

import java.util.List;

/**
 * 描述：： 中原接口
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYuanService
 * @author: it-yml
 * @create: 2023-08-25 19:38
 * @Version 1.0
 * --------------------------------------
 **/
public interface ZhongYuanService {

    /**
     * 时间范围内的中原转化数据获取
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListWithValidityPeriod(String tcId,String apiCode,Long indexId, String requestStartDate,String requestEndDate);

    /**
     * 中原转化数据推Daas
     */
    void zhongYuanTransferDataToDaas(List<MarketingTransferSyncUser> marketingTransferSyncUserList);

    /**
     * 中原转化数据推客服转化过滤
     */
    void zhongYuanTransferDataToCustomerFilter(List<MarketingTransferSyncUser> marketingTransferSyncUserList);
}
