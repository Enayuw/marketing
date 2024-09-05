package com.br.marketing.proxy;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.dto.report.xiecheng.XiechengCollidingDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingWeeklyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengDataRatioDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferMonthlyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferWeeklyReportDTO;

public interface XiechengBiReportService {

    List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyList(String month);

    List<XiechengTransferDailyReportDTO> selectXcTransferDailybI_(String reportDateStart, String reportDateEnd);

    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollList(String reportDateStart, String reportDateEnd);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayList(String reportDateStart, String reportDateEnd);

    List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeekly(String reportDateStart, String reportDateEnd);

    List<XiechengDataRatioDailyReportDTO> selectXcDataRatioList(String reportDateStart);


    List<XiechengDataRatioDailyReportDTO> selectXcDataRatioListbI_(String reportDateStart);


    List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyListbI_(@Param("month") String month);


    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollListbI_(@Param("reportDateStart") String reportDateStart,
                                                                           @Param("reportDateEnd") String reportDateEnd);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayListbI_(@Param("reportDateStart") String reportDateStart,
                                                                               @Param("reportDateEnd") String reportDateEnd);

    List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeeklybI_(@Param("reportDateStart") String reportDateStart,
                                                                      @Param("reportDateEnd") String reportDateEnd);
}
