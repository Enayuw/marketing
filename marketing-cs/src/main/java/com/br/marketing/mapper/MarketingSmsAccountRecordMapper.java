package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingSmsAccountRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSmsAccountRecordMapper extends MarketingSmsAccountRecordMapperBase {

    List<MarketingSmsAccountRecord> selectList(
            @Param("vendorName") String vendorName,
            @Param("channelsName") String channelsName,
            @Param("price") Double price
    );
}