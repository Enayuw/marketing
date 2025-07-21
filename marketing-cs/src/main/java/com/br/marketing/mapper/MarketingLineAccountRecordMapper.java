package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineAccountRecord;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

public interface MarketingLineAccountRecordMapper extends MarketingLineAccountRecordMapperBase {

    List<MarketingLineAccountRecord> getLineAccountsByConfigId(
            @Param("configId") Long configId
    );

    List<MarketingLineAccountRecord> selectList(
            @Param("lineSupplier") String lineSupplier,
            @Param("callerFullName") String callerFullName,
            @Param("price") Double price,
            @Param("nowDate") Date nowDate);

}