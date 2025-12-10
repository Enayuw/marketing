package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailData;
import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessDataMapper extends MarketingTcyrCpaSuccessDataMapperBase {

    void batchSave(@Param("list") List<MarketingTcyrCpaSuccessData> list);

    List<MarketingTcyrCpaSuccessData> selectBySyncFileId(@Param("syncFileId") Long syncFileId, @Param("minId") Long minId);

    Long selectMinIdBySyncFileId(@Param("syncFileId") Long syncFileId);

    Long selectMaxIdBySyncFileId(@Param("syncFileId") Long syncFileId);

    List<MarketingTcyrCpaSuccessData> selectBySyncFileIdAndIdRange(
            @Param("syncFileId") Long syncFileId,
            @Param("startId") Long startId,
            @Param("endId") Long endId,
            @Param("batchSize") Integer batchSize);
}