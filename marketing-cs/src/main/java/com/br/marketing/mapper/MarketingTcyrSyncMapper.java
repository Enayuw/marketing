package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSync;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketingTcyrSyncMapper extends MarketingTcyrSyncMapperBase {

    List<MarketingTcyrSync> selectTcSyncList(@Param("batchNo") String batchNo, @Param("cleanStatus") Integer cleanStatus, @Param("lastSearchId")Long lastSearchId, @Param("searchSize") Integer searchSize);

    Integer updateCleanStatus(@Param("idList") List<Long> idList, @Param("cleanStatus") Integer cleanStatus);

    List<MarketingTcyrSync> selectUnMatchSyncList(@Param("apiCode") String apiCode,@Param("lastSearchId")Long lastSearchId, @Param("searchSize") Integer searchSize);

    Integer batchUpdateMatchInfo(List<MarketingTcyrSync> tcyrSyncList);

    Integer updateMatchInfo(MarketingTcyrSync syncItem);

    //shard-查询apiCode未匹配的数据 不排序不分页
    List<MarketingTcyrSync> selectMatchSyncList(@Param("apiCode") String apiCode, @Param("searchSize") Integer searchSize,
                                                @Param("startSearchTime") LocalDateTime startSearchTime);
    //shard-is_match更改中间态
    Integer updateMiddleMatchStatus(@Param("idList") List<Long> idList);

    //统计单个txt 最后入库id数量
    Long selecFileDbCount(@Param("apiCode") String apiCode,@Param("syncFileId") Long syncFileId);

    void insertDataToDb(@Param("insertDbSql") String insertDbSql);
}