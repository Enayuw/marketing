package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSync;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrSyncMapper extends MarketingTcyrSyncMapperBase {

    List<MarketingTcyrSync> selectTcSyncList(@Param("batchNo") String batchNo, @Param("cleanStatus") Integer cleanStatus, @Param("lastSearchId")Long lastSearchId, @Param("searchSize") Integer searchSize);

    Integer updateCleanStatus(@Param("idList") List<Long> idList, @Param("cleanStatus") Integer cleanStatus);

    Long selectWaitMatchCount(@Param("apiCode") String apiCode);

    Integer dealTcMatch(@Param("apiCode") String apiCode);

    List<MarketingTcyrSync> selectUnMatchSyncList(@Param("apiCode") String apiCode,@Param("lastSearchId")Long lastSearchId, @Param("searchSize") Integer searchSize);

    Integer batchUpdateMatchInfo(List<MarketingTcyrSync> tcyrSyncList);
}