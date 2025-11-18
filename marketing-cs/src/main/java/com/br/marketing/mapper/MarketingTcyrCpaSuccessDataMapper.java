package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessDataMapper extends MarketingTcyrCpaSuccessDataMapperBase {

    void batchSave(@Param("list") List<MarketingTcyrCpaSuccessData> list);

    List<MarketingTcyrCpaSuccessData> selectBySyncFileId(@Param("syncFileId") Long syncFileId, @Param("minId") Long minId);
}