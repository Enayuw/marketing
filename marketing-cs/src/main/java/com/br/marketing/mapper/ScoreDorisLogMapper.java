package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreDorisLog;
import org.apache.ibatis.annotations.Param;

public interface ScoreDorisLogMapper extends ScoreDorisLogMapperBase{


    ScoreDorisLog selectNewestLog(@Param("apiCode") String apiCode);

    String selectNewestBatchNumberLogbI_(@Param("apiCode") String apiCode);

    String selectNewestBatchNumberLog(@Param("apiCode") String apiCode);
}

