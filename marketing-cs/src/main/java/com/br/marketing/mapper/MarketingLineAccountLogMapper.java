package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingLineAccountLog;
import com.br.marketing.entity.MarketingLineAccountRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingLineAccountLogMapper extends MarketingLineAccountLogMapperBase{

    List<MarketingLineAccountLog> getLineAccountLogs(
            @Param("configId") Long configId
    );
}