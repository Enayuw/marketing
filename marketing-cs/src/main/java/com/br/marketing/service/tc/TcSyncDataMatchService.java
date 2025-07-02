package com.br.marketing.service.tc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;

import java.util.List;

/**
 * 同城易融拉取文件数据入库Service
 * @author zhiyong.zhang
 * @date 2025/04/21
 */
public interface TcSyncDataMatchService {

    // 查询未匹配的tcyrSnclist
    List<MarketingTcyrSync> selectUnMatchSyncList(String apiCode,Long lastSearchId, Integer searchSize);

    //match匹配
    void matchTcyrSyncList(String apiCode,List<MarketingTcyrSync> tcyrSyncList);

    void processUnMatchSingleData(String apiCode, MarketingTcyrSync tcyrSync);

    void shardProcess(String apiCode);
}
