package com.br.marketing.proxy;

import java.util.List;

import com.br.marketing.dto.report.xiecheng.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.br.marketing.common.annoation.PercentConvertor;
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
    public List<XiechengTransferMonthlySwitchOnReportDTO> selectXcTrabsferMonthlySwitchOnList(String month) {
        return xieChengBiReportMapper.selectXcTrabsferMonthlySwitchOnListbI_(month);
    }

    @Override
    public List<XiechengTransferDailyReportDTO> selectXcTransferDaily(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcTransferDailybI_(reportDateStart, reportDateEnd);
    }

    @Override
    public List<XiechengTransferDailySwitchOnReportDTO> selectXcTransferDailySwitchOn(String reportDateStart, String reportDateEnd) {
        return xieChengBiReportMapper.selectXcTransferDailySwitchOnbI_(reportDateStart, reportDateEnd);
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
    public List<XiechengDataRatioDailyReportDTO> selectXcDataRatioList(String reportDateStart) {
        return xieChengBiReportMapper.selectXcDataRatioListbI_(reportDateStart);
    }

}
