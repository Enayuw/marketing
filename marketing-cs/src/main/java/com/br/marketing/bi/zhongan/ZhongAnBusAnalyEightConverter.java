package com.br.marketing.bi.zhongan;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyEightReportDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.BUSINESS_ANALYSIS_EIGHT_REPORT)
public class ZhongAnBusAnalyEightConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnBusAnalyEightReportDTO> {

    @Autowired
    ZhongAnBiReportService zhongAnBiReportService;

    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Override
    public List<ZhongAnBusAnalyEightReportDTO> fetchData(BiReportParam param) {
        String reportDate = param.getCondition().getString("reportDate");
        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        if (ObjectUtil.isNotEmpty(reportDate)) {
            reportStatisticTransferExample.createCriteria()
                    .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue().toString())
                    .andReportDateEqualTo(reportDate)
                    .andReportStatusEqualTo("0");

        } else {
            reportDate = LocalDate.now().toString();
            reportStatisticTransferExample.createCriteria()
                    .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue().toString())
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
        List<ZhongAnBusAnalyEightReportDTO> zhongAnBusAnalyEightReportDTOList = zhongAnBiReportService.selectZaBusAnalyEightListbI_(reportId);
        return zhongAnBusAnalyEightReportDTOList;
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnBusAnalyEightReportDTO> sortedData, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.BUSINESS_ANALYSIS_EIGHT_REPORT.getTypeName());
        biReportVO.setReportName("场景八经营分析报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
       /* List<ZhongAnBusAnalyEightReportDTO> sortedData = dtos.stream()
                .sorted(Comparator.comparing(ZhongAnBusAnalyEightReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());*/
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(ZhongAnBusAnalyEightReportDTO::getReportDate).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("观测日", sortedData, ZhongAnBusAnalyEightReportDTO::getQueryDate, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("组别", sortedData, ZhongAnBusAnalyEightReportDTO::getConstituencies, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("总数据量", sortedData, ZhongAnBusAnalyEightReportDTO::getTotalNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("进件人数", sortedData, ZhongAnBusAnalyEightReportDTO::getIncomingNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("进件增量提升率", sortedData, ZhongAnBusAnalyEightReportDTO::getIncomingIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("进件穿透率", sortedData, ZhongAnBusAnalyEightReportDTO::getIncomingTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核人数", sortedData, ZhongAnBusAnalyEightReportDTO::getApproversNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("批核通过率", sortedData, ZhongAnBusAnalyEightReportDTO::getApproversRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核增量提升率", sortedData, ZhongAnBusAnalyEightReportDTO::getApproversIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核通过穿透率", sortedData, ZhongAnBusAnalyEightReportDTO::getApproversTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("综合增量件数", sortedData, ZhongAnBusAnalyEightReportDTO::getCompositeIncrNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("成本", sortedData, ZhongAnBusAnalyEightReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("收入", sortedData, ZhongAnBusAnalyEightReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        //yAxis.add(buildWrapDataVO("收入总计", sortedData, ZhongAnBusAnalyEightReportDTO::getIncomeTotal, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("ROI", sortedData, ZhongAnBusAnalyEightReportDTO::getRoi, FormatType.THOUSAND_SEPARATOR_DECIMAL));

        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;

    }
}
