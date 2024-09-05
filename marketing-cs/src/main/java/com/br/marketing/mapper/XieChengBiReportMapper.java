package com.br.marketing.mapper;

import com.br.marketing.dto.report.xiecheng.XiechengCollidingDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingWeeklyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferWeeklyReportDTO;
import com.br.marketing.entity.DwsXcDataRatioD;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengBiReportMapper {
    List<DwsXcDataRatioD> selectXcDataRatioListbI_(@Param("reportDateStart") String reportDateStart);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayListbI_(@Param("reportDateStart") String reportDateStart, @Param(
            "reportDateEnd") String reportDateEnd);

    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollListbI_(@Param("reportDateStart") String reportDateStart,
                                                                           @Param("reportDateEnd") String reportDateEnd);

    List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeeklybI_(@Param("reportDateStart") String reportDateStart,
                                                                      @Param("reportDateEnd") String reportDateEnd);

    List<XiechengTransferDailyReportDTO> selectXcTransferDailybI_(@Param("reportDateStart") String reportDateStart,
                                                                  @Param("reportDateEnd") String reportDateEnd);
}
