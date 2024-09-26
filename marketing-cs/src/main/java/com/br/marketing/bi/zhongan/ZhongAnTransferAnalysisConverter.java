package com.br.marketing.bi.zhongan;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ReportStatisticTransferDetail;
import com.br.marketing.entity.ReportFieldMapping;
import com.br.marketing.entity.ReportFieldMappingExample;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportFieldMappingMapper;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
public class ZhongAnTransferAnalysisConverter extends AbstractBiReportConverter<BiReportVO, ReportStatisticTransferDetail> {

    @Resource
    private ZhongAnBiReportMapper zhongAnBiReportMapper;

    @Resource
    private ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Resource
    private ReportFieldMappingMapper reportFieldMappingMapper;

    @Override
    public List<ReportStatisticTransferDetail> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");

        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        reportStatisticTransferExample.createCriteria().andReportTaskIdEqualTo(taskId);
        reportStatisticTransferExample.setOrderByClause("create_time desc");
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

        String taskId = extend.getString("taskId");
        String reportId = extend.getString("reportId");
        String scoreFieldStr = extend.getString("scoreField");
        String dimensionField = extend.getString("dimensionField");
        String dimensionValueStr = extend.getString("dimensionValue");

        ReportFieldMappingExample reportFieldDictExample = new ReportFieldMappingExample();
        reportFieldDictExample.createCriteria().andReportTaskIdEqualTo(taskId);
        reportFieldDictExample.setOrderByClause("item_order asc");
        List<ReportFieldMapping> reportFieldMappingList = reportFieldMappingMapper.selectByExample(reportFieldDictExample);

        JSONArray scoreFieldJa = JSONObject.parseArray(scoreFieldStr);
        JSONArray dimensionValueJa = JSONObject.parseArray(dimensionValueStr);
        for(Object scoreFieldObj : scoreFieldJa){
            JSONObject scoreFieldJo = (JSONObject) scoreFieldObj;
            String scoreField = scoreFieldJo.getString("field");
            for(Object dimensionValueObj : dimensionValueJa) {
                String dimensionValue = String.valueOf(dimensionValueObj);

                BiReportVO biReportVO = new BiReportVO();
                biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT.getTypeName());
                biReportVO.setReportName("转化分析报表-"+scoreField+"-"+dimensionValue);
                biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());

                List<ReportStatisticTransferDetail> reportDataList = zhongAnBiReportMapper.queryReportStatisticTransferDetailbI_(reportId, "", "", "", "");

                List<ReportStatisticTransferDetail> caseList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "案件量");
                caseList = caseList.stream().sorted(Comparator.comparing((data) -> {
                    String scoreValue = data.getScoreValue();
                    String value = scoreValue.split(",")[0].substring(1);
                    return Integer.parseInt(value);
                })).collect(Collectors.toList());

                // 构造横坐标数据
                List<String> xAxis = caseList.stream().map(ReportStatisticTransferDetail::getScoreValue).distinct().collect(Collectors.toList());
                xAxis.add("总计");
                biReportVO.setXAxisName("分值区间");
                biReportVO.setXAxis(xAxis);

                // 构造纵坐标数据
                List<WrapDataVO> yAxis = Lists.newArrayList();

                for(ReportFieldMapping reportFieldMapping: reportFieldMappingList){
                    String itemName = reportFieldMapping.getItemName();
                    String itemShow = reportFieldMapping.getItemShow();
                    String formatTypeName = reportFieldMapping.getItemFormatType();
                    FormatType formatType = FormatType.getByName(formatTypeName);
                    List<ReportStatisticTransferDetail> detailList = filter(reportDataList, scoreField, dimensionField, dimensionValue, itemName);
                    // sort
                    detailList = detailList.stream().sorted(Comparator.comparing((data) -> {
                        String scoreValue = data.getScoreValue();
                        String value = scoreValue.split(",")[0].substring(1);
                        return Integer.parseInt(value);
                    })).collect(Collectors.toList());
                    // sum
                    BigDecimal sumValue = detailList.stream().map(data-> {
                        BigDecimal itemValueDecimal = new BigDecimal(String.valueOf(data.getItemValue()));
                        return itemValueDecimal;
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    ReportStatisticTransferDetail sumDetail = new ReportStatisticTransferDetail();
                    sumDetail.setItemValue(sumValue.toString());
                    detailList.add(sumDetail);
                    yAxis.add(buildWrapDataVO(itemShow, detailList, ReportStatisticTransferDetail::getItemValue, formatType));
                }

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

    public static void main(String[] args) {
        String s1 = NumberUtil.formatPercent(new BigDecimal("123456.123456").doubleValue(), 1);
        System.out.println(s1);
        String s2 = NumberUtil.decimalFormat(",###.00", new BigDecimal("123456.123456").doubleValue());
        System.out.println(s2);
    }
}
