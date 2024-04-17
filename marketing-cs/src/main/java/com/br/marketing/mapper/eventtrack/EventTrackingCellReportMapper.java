package com.br.marketing.mapper.eventtrack;


import com.br.marketing.entity.eventtrack.EventTrackingCellReport;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportCount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventTrackingCellReportMapper extends EventTrackingCellReportMapperBase {

    List<EventTrackingCellReportCount> selectCellReportList(@Param("current") int current, @Param("size") int size
            , @Param("startTime") String startTime, @Param("endTime") String endTime
            , @Param("userName") String userName, @Param("orderField") String orderField
            , @Param("descField") String descField);

    List<EventTrackingCellReport> selectCellReportDetailList(@Param("current") int current, @Param("size") int size
            , @Param("startTime") String startTime, @Param("endTime") String endTime
            , @Param("userName") String userName, @Param("apiCodes") String apiCodes
            , @Param("orderField") String orderField, @Param("descField") String descField);
}