package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import com.br.marketing.entity.MarketingTcyrCpaSuccessDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessDataMapper extends MarketingTcyrCpaSuccessDataMapperBase {

    void insertDataToDb(@Param("insertDbSql") String insertDbSql);

    void batchSave(@Param("list") List<MarketingTcyrCpaSuccessData> list);
}