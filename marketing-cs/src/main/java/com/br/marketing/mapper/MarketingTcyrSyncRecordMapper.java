package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingTcyrSyncRecordMapper extends MarketingTcyrSyncRecordMapperBase{

    List<MarketingTcyrSyncRecord> searchAllTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status);


    List<MarketingTcyrSyncRecord> searchTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status);

    List<MarketingTcyrSyncRecord> searchTcyrSyncListApiCodeNotNull(@Param("status") Integer status);

    List<MarketingTcyrSyncRecord> searchAllTcyrSyncListApiCodeNotNull(@Param("status") Integer status);


    Integer batchAdd(@Param("dataList") List<MarketingTcyrSync> dataList);

    List<Long> selectLastUserRecordIdList(@Param("apiCode")String apiCode,@Param("userKeyList") List<String> userKeyList);

    Integer updateTcyrRecordDownStatus(@Param("batchNo") String batchNo, @Param("downStatus") Integer downStatus);


    List<Map<String, String>> selectLastCustNumCelltikv_ (@Param("apiCode") String apiCode ,@Param("userKeyList") List<String> userKeyList);

    String selectSingleLastCustNumCelltikv_ (@Param("apiCode") String apiCode ,@Param("userKey")String userKey);

    String selectLatestSceneByBatchNo(@Param("apiCode") String apiCode, @Param("batchNo") String batchNo);

    /**
     * 同程标准链路：按 batch_no 取最新一条 sync_record（不依赖 api_code），用于转化/撤销归因。
     */
    MarketingTcyrSyncRecord selectLatestByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 同程拆 apiCode：待灵霄 Agent 匹配的记录（batch_no 不重复，接入成功且 api_code 为空）。
     */
    List<MarketingTcyrSyncRecord> selectPendingApiCodeMatchRecords(@Param("limit") int limit);

    Integer countTodayByApiCodeAndBatchPrefix(@Param("apiCode") String apiCode, @Param("batchPrefix") String batchPrefix);

}