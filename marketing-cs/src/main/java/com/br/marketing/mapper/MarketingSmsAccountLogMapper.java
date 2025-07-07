package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountLogExample;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSmsAccountLogMapper extends MarketingSmsAccountLogMapperBase{

    List<MarketingSmsAccountLog> selectSmsAccountLogs(
            @Param("recordId") Long recordId,
            @Param("vendorName")String vendorName,
            @Param("optUserName")String optUserName,
            @Param("optType")Integer optType
    );
}