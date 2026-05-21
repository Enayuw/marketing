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

    /**
     * 上传数据清洗：仅处理已写入 api_code 的接入成功记录（与 downFile 一致）。
     */
    List<MarketingTcyrSyncRecord> searchAllTcyrSyncListApiCodeNotNull(Integer status);
}
