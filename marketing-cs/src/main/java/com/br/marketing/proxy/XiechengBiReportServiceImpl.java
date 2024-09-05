package com.br.marketing.proxy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.br.marketing.common.annoation.PercentConvertor;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingWeeklyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengDataRatioDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferMonthlyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferWeeklyReportDTO;
import com.br.marketing.mapper.XieChengBiReportMapper;

@Component
@PercentConvertor
public class XiechengBiReportServiceImpl implements XiechengBiReportService{

    @Autowired
    private XieChengBiReportMapper xieChengBiReportMapper;

    @Override
    public List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyList(String month) {
        return xieChengBiReportMapper.selectXcTrabsferMonthlyListbI_(month);
    }

    @Override
    public List<XiechengTransferDailyReportDTO> selectXcTransferDailybI_(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcTransferDailybI_(reportDateStart, reportDateEnd);
    }

    @Override
    public List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollList(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcTransferSevenRollListbI_(reportDateStart, reportDateEnd);
    }

    @Override
    public List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayList(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcColldingDistrubuteDayListbI_(reportDateStart, reportDateEnd);
    }

    @Override
    public List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeekly(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcCollidingWeeklybI_(reportDateStart, reportDateEnd);
    }

    @Override
    public List<XiechengDataRatioDailyReportDTO> selectXcDataRatioList(String reportDateStart) {
        return xieChengBiReportMapper.selectXcDataRatioListbI_(reportDateStart);
    }


    @Override
    public List<XiechengDataRatioDailyReportDTO> selectXcDataRatioListbI_(String reportDateStart) {
        return xieChengBiReportMapper.selectXcDataRatioListbI_(reportDateStart);
    }

    @Override
    public List<XiechengTransferMonthlyReportDTO> selectXcTrabsferMonthlyListbI_(String month) {
        return xieChengBiReportMapper.selectXcTrabsferMonthlyListbI_(month);
    }

    @Override
    public List<XiechengTransferWeeklyReportDTO> selectXcTransferSevenRollListbI_(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcTransferSevenRollListbI_(reportDateStart,reportDateEnd);
    }

    @Override
    public List<XiechengCollidingDailyReportDTO> selectXcColldingDistrubuteDayListbI_(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcColldingDistrubuteDayListbI_(reportDateStart,reportDateEnd);
    }

    @Override
    public List<XiechengCollidingWeeklyReportDTO> selectXcCollidingWeeklybI_(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcCollidingWeeklybI_(reportDateStart,reportDateEnd);
    }


}
