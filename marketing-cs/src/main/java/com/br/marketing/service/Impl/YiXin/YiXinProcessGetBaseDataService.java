package com.br.marketing.service.Impl.YiXin;

import com.br.marketing.entity.MarketingTransferSyncUser;

import java.util.List;

/**
 * 宜信基础数据获取接口
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:38
 */
public interface YiXinProcessGetBaseDataService {

    /**
     * 情况 a 基础数据获取接口
     * @return
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_A();

    /**
     * 情况 b 基础数据获取接口
     * @return
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_B();

    /**
     * 情况 c~i 基础数据获取接口
     * @param actionType c d e f g h i
     * @return
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_B(String actionType);
}
