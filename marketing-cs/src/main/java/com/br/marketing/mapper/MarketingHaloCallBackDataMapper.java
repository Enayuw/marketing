package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingHaloCallBackData;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketingHaloCallBackDataMapper extends MarketingHaloCallBackDataMapperBase {

    List<MarketingHaloCallBackData> selectDataList(@Param("apiCode") String apiCode,
                                                   @Param("dealStatus") Integer dealStatus,
                                                   @Param("searchSeize") Integer searchSize
    );


    void updateDealStatusByIdList(@Param("idList") List<Long> idList,
                                  @Param("dealStatus") Integer dealStatus,
                                  @Param("extend")String extend
    );

}