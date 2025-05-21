package com.br.marketing.service.tc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.entity.MarketingTcyrTransferRecord;

import java.util.List;

/**
 * 同程转化数据清洗任务 service
 * @author zhiyong.zhang
 * @date 2025/04/21
 */
public interface TcTransferRecordService {


    List<MarketingTcyrTransferRecord> selectTcyrTransforRecordList(String apiCode, Integer status, Long lastSearchId, Integer searchSize);

    Integer updateCleanStatus(List<Long> idList, Integer value);
}
