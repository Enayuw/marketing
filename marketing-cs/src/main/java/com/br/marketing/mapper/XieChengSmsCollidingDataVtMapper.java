package com.br.marketing.mapper;


import com.br.marketing.entity.XieChengSmsCollidingDataVt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataVtMapper extends XieChengSmsCollidingDataVtMapperBase{
    List<XieChengSmsCollidingDataVt> selectByLocalIdVttikv_(
                                                            @Param("indexId") Long indexId,
                                                            @Param("localId") Long localId,
                                                            @Param("sendDate") Integer sendDate,
                                                            @Param("xieChengSmsCollidingDataVtPageSize") Integer xieChengSmsCollidingDataVtPageSize);

    Boolean selectMaxNextPushTimetikv_(@Param("sha256Tel") String sha256Tel);
}