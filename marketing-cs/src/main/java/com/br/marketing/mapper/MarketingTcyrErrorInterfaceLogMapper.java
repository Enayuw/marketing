package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrErrorInterfaceLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrErrorInterfaceLogMapper extends MarketingTcyrErrorInterfaceLogMapperBase{

    List<MarketingTcyrErrorInterfaceLog> selectNoDealList(
            @Param("apiCode") String apiCode,
            @Param("searchSize") Integer searchSize);

    void updateDealStatus(
            @Param("id") Long id,
            @Param("dealStatus") Integer dealStatus);

    void batchUpdateDealStatus(
            @Param("idList") List<Long> idList,
            @Param("dealStatus") Integer dealStatus);
}