package com.br.marketing.bi.zhongan;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhongAnTransferAnalysisReportDTO;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.proxy.ZhongAnBiReportService;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName ZhongAnTransferAnalysisConverter
 * @Description 转化分析报表
 * @Author LiXiang
 * @Date 2024-09-23
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT)
public class ZhongAnTransferAnalysisConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnTransferAnalysisReportDTO> {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;

    @Autowired
    ZhongAnBiReportService zhongAnBiReportService;

    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Override
    public List<ZhongAnTransferAnalysisReportDTO> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");

        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        reportStatisticTransferExample.createCriteria().andReportTaskIdEqualTo(taskId);
        List<ReportStatisticTransfer> reportStatisticTransfers = reportStatisticTransferMapper.selectByExample(reportStatisticTransferExample);
        if (reportStatisticTransfers.isEmpty()) {
            return new ArrayList<>();
        }

        ReportStatisticTransfer reportStatisticTransfer = reportStatisticTransfers.get(0);
        String reportId = reportStatisticTransfer.getReportId();
        List<ZhongAnTransferAnalysisReportDTO> dataList = zhongAnBiReportMapper.selectZaItemListbI_(reportId, "", "", "");
        return dataList;
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnTransferAnalysisReportDTO> dataList, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT.getTypeName());
        biReportVO.setReportName("转化分析报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());

        // 构造横坐标数据
        List<String> xAxis = dataList.stream().map(ZhongAnTransferAnalysisReportDTO::getReportDate).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("案件量", dataList, ZhongAnTransferAnalysisReportDTO::getConstituencies, FormatType.DEFAULT));
        yAxis.add(buildWrapDataVO("评分分布", dataList, ZhongAnTransferAnalysisReportDTO::getTotalNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录量", dataList, ZhongAnTransferAnalysisReportDTO::getIncomingNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录率", dataList, ZhongAnTransferAnalysisReportDTO::getIncomingIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请授信量", dataList, ZhongAnTransferAnalysisReportDTO::getIncomingTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请授信穿透率", dataList, ZhongAnTransferAnalysisReportDTO::getApproversNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("授信成功量", dataList, ZhongAnTransferAnalysisReportDTO::getApproversRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信成功率", dataList, ZhongAnTransferAnalysisReportDTO::getApproversIncreaseRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信成功穿透率", dataList, ZhongAnTransferAnalysisReportDTO::getApproversTotalRate, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("批核穿透率", dataList, ZhongAnTransferAnalysisReportDTO::getCompositeIncrNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("批核转化占比", dataList, ZhongAnTransferAnalysisReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));

        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;

    }
}
