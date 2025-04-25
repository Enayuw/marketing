package com.br.marketing.service.tc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 同程上传数据清洗任务
 * @author zhiyong.zhang
 * @date 2025/04/21
 */
public interface TcSyncDataCleanService {

    List<MarketingTcyrSync> selectTcSyncList(String batchNo, Integer cleanStatus,Long lastSearchId,Integer searchSize);

    //修改单个syncRecord的处理结果
    Integer updageTcyrRecordSyncStatus(String batchNo, Integer status);


    Integer updateCleanStatus(List<Long> idList, Integer cleanStatus);

    List<MarketingTcyrSyncRecord> searchAllTcyrSyncList(String apiCode, Integer status);
}
