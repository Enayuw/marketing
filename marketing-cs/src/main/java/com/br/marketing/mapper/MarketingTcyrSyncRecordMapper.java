package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingTcyrSyncRecordMapper extends MarketingTcyrSyncRecordMapperBase{

    List<MarketingTcyrSyncRecord> searchAllTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status);


    List<MarketingTcyrSyncRecord> searchTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status);



    Integer batchAdd(@Param("dataList") List<MarketingTcyrSync> dataList);

    List<Long> selectLastUserRecordIdList(@Param("apiCode")String apiCode,@Param("userKeyList") List<String> userKeyList);

    Integer updateTcyrRecordDownStatus(@Param("batchNo") String batchNo, @Param("downStatus") Integer downStatus);


    List<Map<String, String>> selectLastCustNumCelltikv_ (@Param("apiCode") String apiCode ,@Param("userKeyList") List<String> userKeyList);

    String selectSingleLastCustNumCelltikv_ (@Param("apiCode") String apiCode ,@Param("userKey")String userKey);

    String selectLatestSceneByBatchNo(@Param("apiCode") String apiCode, @Param("batchNo") String batchNo);

    Integer countTodayByApiCodeAndBatchPrefix(@Param("apiCode") String apiCode, @Param("batchPrefix") String batchPrefix);

}