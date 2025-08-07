package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncLabel;
import com.br.marketing.entity.QifuActuation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingSyncLabelMapper {

    int batchInsert(@Param("apiCode") String apiCode,@Param("list") List<MarketingSyncLabel> list);



    List<Map<String,String>> getLabelNum(@Param("labelId") Long labelId,@Param("apiCode") String apiCode);



}
