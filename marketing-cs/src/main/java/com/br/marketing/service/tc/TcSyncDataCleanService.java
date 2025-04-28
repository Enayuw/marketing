package com.br.marketing.service.tc;

import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;

import java.util.List;

/**
 * 同程上传数据清洗任务
 * @author zhiyong.zhang
 * @date 2025/04/21
 */
public interface TcSyncDataCleanService {

    List<MarketingTcyrSync> selectTcSyncList(String batchNo, Integer cleanStatus,Long lastSearchId,Integer searchSize);

    Integer updateCleanStatus(List<Long> idList, Integer cleanStatus);

    List<MarketingTcyrSyncRecord> searchAllTcyrSyncList(String apiCode, Integer status);
}
