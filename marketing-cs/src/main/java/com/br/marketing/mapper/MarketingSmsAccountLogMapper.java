package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSmsAccountLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSmsAccountLogMapper extends MarketingSmsAccountLogMapperBase{

    List<MarketingSmsAccountLog> selectSmsAccountLogs(
            @Param("configId") Long configId);

}