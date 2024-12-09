package com.br.marketing.bi.zhongan;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalySevenReportDTO;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;

import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.proxy.ZhongAnBiReportService;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.BUSINESS_ANALYSIS_SEVEN_REPORT)
public class ZhongAnBusAnalySevenConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnBusAnalySevenReportDTO> {

    @Autowired
    ZhongAnBiReportService zhongAnBiReportService;

    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Override
    public List<ZhongAnBusAnalySevenReportDTO> fetchData(BiReportParam param) {
        String reportDate = param.getCondition().getString("reportDate");
        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        if (ObjectUtil.isNotEmpty(reportDate)) {
            reportStatisticTransferExample.createCriteria()
                    .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue().toString())
                    .andReportDateEqualTo(reportDate)
                    .andReportStatusEqualTo("0");

        } else {
            reportDate = LocalDate.now().toString();
            reportStatisticTransferExample.createCriteria()
                    .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue().toString())
                    .andReportDateEqualTo(reportDate)
                    .andReportStatusEqualTo("0");

        }
        reportStatisticTransferExample.setOrderByClause("create_time desc");
        List<ReportStatisticTransfer> reportStatisticTransfers = reportStatisticTransferMapper.selectByExample(reportStatisticTransferExample);
        if (reportStatisticTransfers.isEmpty()) {
            return new ArrayList<>();
        }

        ReportStatisticTransfer reportStatisticTransfer = reportStatisticTransfers.get(0);
        String reportId = reportStatisticTransfer.getReportId();
        List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalySevenReportDTOList = zhongAnBiReportService.selectZaBusAnalySeveListbI_(reportId);
        return zhongAnBusAnalySevenReportDTOList;
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnBusAnalySevenReportDTO> sortedData, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.BUSINESS_ANALYSIS_SEVEN_REPORT.getTypeName());
        biReportVO.setReportName("场景七经营分析报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        /*List<ZhongAnBusAnalySevenReportDTO> sortedData = dtos.stream()
                .sorted(Comparator.comparing(ZhongAnBusAnalySevenReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());*/
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(ZhongAnBusAnalySevenReportDTO::getReportDate).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("观测日", sortedData, ZhongAnBusAnalySevenReportDTO::getQueryDate, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("客群组别", sortedData, ZhongAnBusAnalySevenReportDTO::getConstituencies, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("数据量", sortedData, ZhongAnBusAnalySevenReportDTO::getTotalNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录率", sortedData, ZhongAnBusAnalySevenReportDTO::getLoginRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("登录量", sortedData, ZhongAnBusAnalySevenReportDTO::getLoginNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("进件人数", sortedData, ZhongAnBusAnalySevenReportDTO::getIncomingNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("进件穿透率", sortedData, ZhongAnBusAnalySevenReportDTO::getIncomingTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("进件增量提升率", sortedData, ZhongAnBusAnalySevenReportDTO::getIncomingIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核人数", sortedData, ZhongAnBusAnalySevenReportDTO::getApproversNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("批核通过率", sortedData, ZhongAnBusAnalySevenReportDTO::getApproversRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核通过穿透率", sortedData, ZhongAnBusAnalySevenReportDTO::getApproversTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核通过穿透率提升比", sortedData, ZhongAnBusAnalySevenReportDTO::getApproversIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("增量批核人数", sortedData, ZhongAnBusAnalySevenReportDTO::getApproversIncrNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("批核件均", sortedData, ZhongAnBusAnalySevenReportDTO::getApprovalsAvgNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("发起提现人数", sortedData, ZhongAnBusAnalySevenReportDTO::getApplyPayNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("发起提现率", sortedData, ZhongAnBusAnalySevenReportDTO::getApplyPayRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("发起提现率提升比", sortedData, ZhongAnBusAnalySevenReportDTO::getApplyPayIncrRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现成功人数", sortedData, ZhongAnBusAnalySevenReportDTO::getApplyPaySuccessNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("放款成功人数", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现通过通过率", sortedData, ZhongAnBusAnalySevenReportDTO::getApplyPaySuccessRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("放款成功率", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核放款穿透率", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersApproversRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("放款成功金额", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucAmount, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("增量放款金额", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucIncrAmount, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("放款成功穿透率", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("放款成功穿透率提升比", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucIncrRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("放款人均", sortedData, ZhongAnBusAnalySevenReportDTO::getLendersSucAvgAmount, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("名单产能", sortedData, ZhongAnBusAnalySevenReportDTO::getProductCapacity, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("收入", sortedData, ZhongAnBusAnalySevenReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("成本", sortedData, ZhongAnBusAnalySevenReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("ROI", sortedData, ZhongAnBusAnalySevenReportDTO::getRoi, FormatType.THOUSAND_SEPARATOR_DECIMAL));

        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }
}
