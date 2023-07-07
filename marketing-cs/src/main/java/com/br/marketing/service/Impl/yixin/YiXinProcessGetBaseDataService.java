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
     * 根据id 进行分页
     * 每页查询2000数据
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCustNumA(String tCid,String apiCode,Long indexId,String requestDate);
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCustNum(String tCid,String apiCode, String type, Long indexId,String requestDate);
    /**
     * 情况 a 基础数据获取接口
     * T-1 日的转化数据，并且 transformType=1 并且 liveType in (4,6) 并且 custNum 去重，去重后去 inserttime 最新的一条数据。
     * 并且根据custNum 过滤
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListA(String cid, String apiCode,String requestDate,List<MarketingTransferSyncUser> marketingTransferSyncUserList);

    /**
     * 情况 b  c~i 基础数据获取接口
     * 获取日期为 T-30 日 (31-30=1 即 31 号的基础数据为 1 号的转化数据)，并且1 号的转化数据中 transformType!=1并能 type =12 。
     * T 日的转化数据 transformType!=1 并且 type=(13,23,20,21,8,15,6) 并且根据 insertTime 取最新的一条数据。
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListBtoCtoI(String cid, String apiCode,String type, String requestDate,List<MarketingTransferSyncUser> marketingTransferSyncUserList);


}
