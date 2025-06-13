package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSyncFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrSyncFileMapper extends MarketingTcyrSyncFileMapperBase{

    MarketingTcyrSyncFile selectSyncFile(
            @Param("apiCode") String apiCode,
            @Param("dealStatus") Integer dealStatus
    );


}