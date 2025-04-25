package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MarketingTcyrSyncRecordMapper extends MarketingTcyrSyncRecordMapperBase{

    List<MarketingTcyrSyncRecord> searchAllTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status);


    List<MarketingTcyrSyncRecord> searchTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status,@Param("dayBeginTime") Date dayBeginTime, @Param("dayEndTime") Date dayEndTime);


    List<Map<String, String>> selectLastUserCellList (@Param("apiCode") String apiCode ,@Param("userKeyList") List<String> userKeyList);

    Integer batchAdd(@Param("dataList") List<MarketingTcyrSync> dataList);

    List<Long> selectLastUserRecordIdList(@Param("apiCode")String apiCode,@Param("userKeyList") List<String> userKeyList);

    List<Map<String, String>> selectLastUserRecordList(@Param("apiCode")String apiCode,@Param("idList") List<Long> idList);

    Integer updageTcyrRecordSyncStatus(@Param("batchNo") String batchNo, @Param("status") Integer status);

    Integer updageTcyrRecordDownStatus(@Param("batchNo") String batchNo, @Param("downStatus") Integer downStatus);
}