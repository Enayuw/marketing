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


    //处理单个同城易融具体批次batchNo的文件加载和同步db
    Result dealTcyrFileSync(MarketingTcyrSyncRecord syncRecord);

    //修改单个syncRecord的处理结果
    Integer updageTcyrRecordSyncStatus(String batchNo, Integer status);

    //查询apiCode对应的待匹配的条数
    Long selectWaitMatchCount(String apiCode);

    // sql 直接处理同城match
    Integer dealTcMatch(String apiCode);

    // 查询未匹配的tcyrSnclist
    List<MarketingTcyrSync> selectUnMatchSyncList(String apiCode,Long lastSearchId, Integer searchSize);

    //match匹配
    void matchTcyrSyncList(String apiCode,List<MarketingTcyrSync> tcyrSyncList);
}
