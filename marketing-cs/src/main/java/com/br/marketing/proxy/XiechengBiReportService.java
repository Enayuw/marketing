package com.br.marketing.proxy;

import com.br.marketing.dto.report.xiecheng.*;
import java.util.List;

public interface XiechengBiReportService {

    List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyList(String month);

    List<XiechengTransferDailyReportDTO> selectXcTransferDailybI_(String reportDateStart, String reportDateEnd);

    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollList(String reportDateStart, String reportDateEnd);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayList(String reportDateStart, String reportDateEnd);

    List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeekly(String reportDateStart, String reportDateEnd);

    List<XiechengDataRatioDailyReportDTO> selectXcDataRatioList(String reportDateStart);



}
