package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailData;
import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import com.br.marketing.entity.WubaCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailDataMapper extends MarketingTcyrCpaFailDataMapperBase {

    void batchSave(@Param("list") List<MarketingTcyrCpaFailData> list);

    List<MarketingTcyrCpaFailData> selectBySyncFileId(@Param("syncFileId") Long syncFileId, @Param("minId") Long minId);

    Long selectMinIdBySyncFileId(@Param("syncFileId") Long syncFileId);

    Long selectMaxIdBySyncFileId(@Param("syncFileId") Long syncFileId);

    List<MarketingTcyrCpaFailData> selectBySyncFileIdAndIdRange(
            @Param("syncFileId") Long syncFileId,
            @Param("startId") Long startId,
            @Param("endId") Long endId,
            @Param("batchSize") Integer batchSize);

}