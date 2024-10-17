package com.br.marketing.proxy;

import java.util.List;

import com.br.marketing.dto.report.xiecheng.*;
import org.apache.ibatis.annotations.Param;

public interface XiechengBiReportService {

    List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyList(String month);

    List<XiechengTransferMonthlySwitchOnReportDTO> selectXcTrabsferMonthlySwitchOnList(String month);

    List<XiechengTransferDailyReportDTO> selectXcTransferDaily(String reportDateStart, String reportDateEnd);

    List<XiechengTransferDailySwitchOnReportDTO> selectXcTransferDailySwitchOn(String reportDateStart, String reportDateEnd);

    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollList(String reportDateStart, String reportDateEnd);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayList(String reportDateStart, String reportDateEnd);

    List<XiechengDataRatioDailyReportDTO> selectXcDataRatioList(String reportDateStart);
}
