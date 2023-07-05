package com.br.marketing.service.Impl.yixin;

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
     * T-1 日的转化数据，并且 transformType=1 并且 liveType in (4,6) 并且 custNum 去重，去重后去 inserttime 最新的一条数据。
     * @param idIndex 循环查询的最大 id
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListA(String cid,String type,Integer pageNum);

    /**
     * 情况 b 基础数据获取接口
     * 获取日期为 T-30 日 (31-30=1 即 31 号的基础数据为 1 号的转化数据)，并且1 号的转化数据中 transformType!=1并能 type =12 。
     * @param idIndex 循环查询的最大 id
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListB(String cid,String type,Integer pageNum);

    /**
     * 情况 c~i 基础数据获取接口
     * T 日的转化数据 transformType!=1 并且 type=(13,23,20,21,8,15,6) 并且根据 insertTime 取最新的一条数据。
     * @param type 13 23 25
     * @param idIndex 循环查询的最大 id
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCtoI(String cid,String type,Integer pageNum);
}
