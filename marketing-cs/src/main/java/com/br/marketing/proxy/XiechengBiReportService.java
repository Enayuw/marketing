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

    List<XiechengTransferDailyReportDTO> selectXcTransferDaily(String reportDateStart, String reportDateEnd);

    List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollList(String reportDateStart, String reportDateEnd);

    List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayList(String reportDateStart, String reportDateEnd);

    List<XiechengDataRatioDailyReportDTO> selectXcDataRatioList(String reportDateStart);
}
