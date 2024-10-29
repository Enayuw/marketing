package com.br.marketing.mapper;

import com.br.marketing.dto.report.xiecheng.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengBiReportMapper {

    List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyListbI_(@Param("month") String month);

    List<XiechengTransferMonthlySwitchOnReportDTO> selectXcTrabsferMonthlySwitchOnListbI_(@Param("month") String month);

    List<XiechengTransferDailyReportDTO> selectXcTransferDailybI_(@Param("reportDateStart") String reportDateStart,
                                                                  @Param("reportDateEnd") String reportDateEnd);

    List<XiechengTransferDailySwitchOnReportDTO> selectXcTransferDailySwitchOnbI_(@Param("reportDateStart") String reportDateStart,
                                                                  @Param("reportDateEnd") String reportDateEnd);

    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollListbI_(@Param("reportDateStart") String reportDateStart,
                                                                           @Param("reportDateEnd") String reportDateEnd);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayListbI_(@Param("reportDateStart") String reportDateStart,
                                                                               @Param("reportDateEnd") String reportDateEnd);

    List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeeklybI_(@Param("reportDateStart") String reportDateStart,
                                                                      @Param("reportDateEnd") String reportDateEnd);

    List<XiechengDataRatioDailyReportDTO> selectXcDataRatioListbI_(@Param("reportDateStart") String reportDateStart);

    int selectXcColldingSucCountbI_(@Param("reportDate") String reportDate, @Param("reportDateEnd") String reportDateEnd);

}
