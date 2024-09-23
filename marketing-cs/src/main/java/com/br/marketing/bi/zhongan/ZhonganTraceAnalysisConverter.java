package com.br.marketing.bi.zhongan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ReportStatisticTransferDetail;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName ZhonganTraceAnalysisConverter
 * @Description 回朔报表
 * @Author LiXiang
 * @Date 2024-09-23
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.TRACE_ANALYSIS_REPORT)
public class ZhonganTraceAnalysisConverter extends AbstractBiReportConverter<BiReportVO, ReportStatisticTransferDetail> {

    @Resource
    private ZhongAnBiReportMapper zhongAnBiReportMapper;

    @Resource
    private ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Override
    public List<ReportStatisticTransferDetail> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");

        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        reportStatisticTransferExample.createCriteria().andReportTaskIdEqualTo(taskId);
        List<ReportStatisticTransfer> reportStatisticTransfers = reportStatisticTransferMapper.selectByExample(reportStatisticTransferExample);
        if (reportStatisticTransfers.isEmpty()) {
            return new ArrayList<>();
        }

        ReportStatisticTransfer reportStatisticTransfer = reportStatisticTransfers.get(0);
        String reportId = reportStatisticTransfer.getReportId();
        String scoreField = reportStatisticTransfer.getScoreField();
        String dimensionField = reportStatisticTransfer.getDimensionField();
        String dimensionValue = reportStatisticTransfer.getDimensionValue();


        param.getCondition().put("reportId", reportId);
        param.getCondition().put("scoreField", scoreField);
        param.getCondition().put("dimensionField", dimensionField);
        param.getCondition().put("dimensionValue", dimensionValue);
        return new ArrayList<>();
    }

    @Override
    public List<BiReportVO> process(List<ReportStatisticTransferDetail> dataList, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();

        String reportId = extend.getString("reportId");
        String scoreFieldStr = extend.getString("scoreField");
        String dimensionField = extend.getString("dimensionField");
        String dimensionValueStr = extend.getString("dimensionValue");

        JSONArray scoreFieldJa = JSONObject.parseArray(scoreFieldStr);
        JSONArray dimensionValueJa = JSONObject.parseArray(dimensionValueStr);
        for(Object scoreFieldObj : scoreFieldJa){
            JSONObject scoreFieldJo = (JSONObject) scoreFieldObj;
            String scoreField = scoreFieldJo.getString("field");
            for(Object dimensionValueObj : dimensionValueJa) {
                String dimensionValue = String.valueOf(dimensionValueObj);

                BiReportVO biReportVO = new BiReportVO();
                biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT.getTypeName());
                biReportVO.setReportName("转化分析报表");
                biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());

                List<ReportStatisticTransferDetail> reportDataList = zhongAnBiReportMapper.queryReportStatisticTransferDetailbI_(reportId, "", "", "", "");
                List<ReportStatisticTransferDetail> caseList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "案件量");
                List<ReportStatisticTransferDetail> caseRateList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "评分分布");
                List<ReportStatisticTransferDetail> loginList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "登录量");
                List<ReportStatisticTransferDetail> loginRateList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "登录率");
                List<ReportStatisticTransferDetail> applyList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "申请授信量");
                List<ReportStatisticTransferDetail> applyRateList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "申请授信穿透率");
                List<ReportStatisticTransferDetail> creditList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "授信成功量");
                List<ReportStatisticTransferDetail> creditRateList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "授信成功率");
                List<ReportStatisticTransferDetail> creditDistributeRateList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "授信成功穿透率");
                List<ReportStatisticTransferDetail> piheRateResultList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "批核穿透率");
                List<ReportStatisticTransferDetail> piheDistributeRateList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "批核转化占比");

                // 构造横坐标数据
                List<String> xAxis = caseList.stream().map(ReportStatisticTransferDetail::getScoreValue).distinct().collect(Collectors.toList());
                biReportVO.setXAxisName("分值区间");
                biReportVO.setXAxis(xAxis);
                // 构造纵坐标数据
                List<WrapDataVO> yAxis = Lists.newArrayList();
                yAxis.add(buildWrapDataVO("案件量", caseList, ReportStatisticTransferDetail::getItemValue, FormatType.DEFAULT));
                yAxis.add(buildWrapDataVO("评分分布", caseRateList, ReportStatisticTransferDetail::getItemValue, FormatType.THOUSAND_SEPARATOR));
                yAxis.add(buildWrapDataVO("登录量", loginList, ReportStatisticTransferDetail::getItemValue, FormatType.THOUSAND_SEPARATOR));
                yAxis.add(buildWrapDataVO("登录率", loginRateList, ReportStatisticTransferDetail::getItemValue, FormatType.PERCENT_SIGN));
                yAxis.add(buildWrapDataVO("申请授信量", applyList, ReportStatisticTransferDetail::getItemValue, FormatType.PERCENT_SIGN));
                yAxis.add(buildWrapDataVO("申请授信穿透率", applyRateList, ReportStatisticTransferDetail::getItemValue, FormatType.THOUSAND_SEPARATOR));
                yAxis.add(buildWrapDataVO("授信成功量", creditList, ReportStatisticTransferDetail::getItemValue, FormatType.PERCENT_SIGN));
                yAxis.add(buildWrapDataVO("授信成功率", creditRateList, ReportStatisticTransferDetail::getItemValue, FormatType.PERCENT_SIGN));
                yAxis.add(buildWrapDataVO("授信成功穿透率", creditDistributeRateList, ReportStatisticTransferDetail::getItemValue, FormatType.PERCENT_SIGN));
                yAxis.add(buildWrapDataVO("批核穿透率", piheRateResultList, ReportStatisticTransferDetail::getItemValue, FormatType.THOUSAND_SEPARATOR));
                yAxis.add(buildWrapDataVO("批核转化占比", piheDistributeRateList, ReportStatisticTransferDetail::getItemValue, FormatType.THOUSAND_SEPARATOR_DECIMAL));

                biReportVO.setYAxis(yAxis);
                biReportVOList.add(biReportVO);

            }
        }
        return biReportVOList;

    }

    @Override
    public JSONObject buildExtend(BiReportParam param) {
        JSONObject condition = param.getCondition();
        return condition;
    }

    public List<ReportStatisticTransferDetail> filter(List<ReportStatisticTransferDetail> reportDataList, String scoreField, String dimensionField, String dimensionValue, String itemName) {
        List<ReportStatisticTransferDetail> dataList = reportDataList.stream().filter(data -> {
            if(scoreField.equals(data.getScoreField()) && dimensionField.equals(data.getDimensionField())
                    && dimensionValue.equals(data.getDimensionValue()) && itemName.equals(data.getItemName())){
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return dataList;
    }
}
