package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrTransferRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrTransferRecordMapper extends MarketingTcyrTransferRecordMapperBase{

    List<MarketingTcyrTransferRecord> selectTcyrTransforRecordList(@Param("apiCode") String apiCode, @Param("status") Integer status, @Param("lastSearchId")Long lastSearchId, @Param("searchSize") Integer searchSize);

    Integer updateCleanStatus(@Param("idList")List<Long> idList, @Param("cleanStatus")Integer cleanStatus);
}