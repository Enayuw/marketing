package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRetryEs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingRetryEsMapper extends MarketingRetryEsMapperBase {

    List<MarketingRetryEs> queryByDateAndStatus(@Param("fileId") String fileId, @Param("date") String date, @Param("minId") Long minId);

    List<String> queryFileIdGroup(@Param("date") String date);
}