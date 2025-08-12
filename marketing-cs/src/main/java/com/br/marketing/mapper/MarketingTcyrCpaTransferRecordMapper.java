package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaTransferRecord;
import com.br.marketing.entity.MarketingTcyrCpaTransferRecordExample;
import com.br.marketing.entity.MarketingTcyrTransferRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaTransferRecordMapper extends MarketingTcyrCpaTransferRecordMapperBase{
    List<MarketingTcyrCpaTransferRecord> selectTcyrTransforRecordList(
            @Param("apiCode") String apiCode,
            @Param("status") Integer status,
            @Param("lastSearchId")Long lastSearchId,
            @Param("searchSize") Integer searchSize);

    void updateCleanStatus(
            @Param("idList")List<Long> idList,
            @Param("cleanStatus")Integer cleanStatus);
}