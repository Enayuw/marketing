package com.br.marketing.bi.zhongan;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyOneReportDTO;
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
@BiReportType(reportType = BiReportTypeEnum.BUSINESS_ANALYSIS_ONE_REPORT)
public class ZhongAnBusAnalyOneConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnBusAnalyOneReportDTO> {

    @Autowired
    ZhongAnBiReportService zhongAnBiReportService;

    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;


    @Override
    public List<ZhongAnBusAnalyOneReportDTO> fetchData(BiReportParam param) {
        String reportDate = param.getCondition().getString("reportDate");
        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        if (ObjectUtil.isNotEmpty(reportDate)) {
            reportStatisticTransferExample.createCriteria()
                    .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue().toString())
                    .andReportDateEqualTo(reportDate)
                    .andReportStatusEqualTo("0");
        } else {
            reportDate = LocalDate.now().toString();
            reportStatisticTransferExample.createCriteria()
                    .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue().toString())
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
        List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyOneReportDTOList = zhongAnBiReportService.selectZaBusAnalyOneListbI_(reportId);
        return zhongAnBusAnalyOneReportDTOList;
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnBusAnalyOneReportDTO> sortedData, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.BUSINESS_ANALYSIS_ONE_REPORT.getTypeName());
        biReportVO.setReportName("场景一经营分析报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        /*// 根据时间排序
        List<ZhongAnBusAnalyOneReportDTO> sortedData = dtos.stream()
                .sorted(Comparator.comparing(ZhongAnBusAnalyOneReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());*/
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(ZhongAnBusAnalyOneReportDTO::getReportDate).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("观测日", sortedData, ZhongAnBusAnalyOneReportDTO::getQueryDate, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("组别", sortedData, ZhongAnBusAnalyOneReportDTO::getConstituencies, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("总数据量", sortedData, ZhongAnBusAnalyOneReportDTO::getTotalNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("进件人数", sortedData, ZhongAnBusAnalyOneReportDTO::getIncomingNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("进件增量提升率", sortedData, ZhongAnBusAnalyOneReportDTO::getIncomingIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("进件穿透率", sortedData, ZhongAnBusAnalyOneReportDTO::getIncomingTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核人数", sortedData, ZhongAnBusAnalyOneReportDTO::getApproversNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("批核通过率", sortedData, ZhongAnBusAnalyOneReportDTO::getApproversRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核增量提升率", sortedData, ZhongAnBusAnalyOneReportDTO::getApproversIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核通过穿透率", sortedData, ZhongAnBusAnalyOneReportDTO::getApproversTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("综合增量件数", sortedData, ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("成本", sortedData, ZhongAnBusAnalyOneReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("收入", sortedData, ZhongAnBusAnalyOneReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("ROI", sortedData, ZhongAnBusAnalyOneReportDTO::getRoi, FormatType.THOUSAND_SEPARATOR_DECIMAL));

        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }
}
