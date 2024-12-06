package com.br.marketing.bi.zhongan;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.zhongan.ReportStatisticTransferDetail;
import com.br.marketing.entity.*;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportFieldMappingMapper;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.util.DataBarUtil;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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

    @Resource
    private ReportTaskMapper reportTaskMapper;

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
        Map<String, String> sumValueMap = new HashMap<>();
        for(Object scoreFieldObj : scoreFieldJa){
            JSONObject scoreFieldJo = (JSONObject) scoreFieldObj;
            String scoreField = scoreFieldJo.getString("field");
            for(Object dimensionValueObj : dimensionValueJa) {
                String dimensionValue = String.valueOf(dimensionValueObj);

                BiReportVO biReportVO = new BiReportVO();
                biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT.getTypeName());
                String groupName = getGroupName(taskId, dimensionValue);
                biReportVO.setReportName(BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT.getStatName()+"-"+scoreField+"-"+groupName);
                biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());

                List<ReportStatisticTransferDetail> reportDataList = zhongAnBiReportMapper.queryReportStatisticTransferDetailbI_(reportId, "", "", "", "");

                List<ReportStatisticTransferDetail> caseList = filter(reportDataList, scoreField, dimensionField, dimensionValue, "数据量");
                caseList = caseList.stream().sorted(Comparator.comparing((ReportStatisticTransferDetail data) -> {
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
                    detailList = detailList.stream().sorted(Comparator.comparing((ReportStatisticTransferDetail data) -> {
                        String scoreValue = data.getScoreValue();
                        String value = scoreValue.split(",")[0].substring(1);
                        return Integer.parseInt(value);
                    })).collect(Collectors.toList());
                    // sum
                    String sum = calculateSum(itemName, detailList, sumValueMap);
                    ReportStatisticTransferDetail sumDetail = new ReportStatisticTransferDetail();
                    sumDetail.setItemValue(sum);
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

    /**
     * 导出数据
     *
     * @param excelWriter excelWriter
     * @param params 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    @Override
    public void exportData(ExcelWriter excelWriter, List<BiReportDownLoadParam> params) {
        Map<String, List<BiReportDownLoadParam>> mapByGroupName = params.stream()
                .collect(Collectors.groupingBy((BiReportDownLoadParam param) -> {
                    String reportName = param.getReportName();
                    String[] reportNameSplit = reportName.split("-");
                    String groupName = reportNameSplit[reportNameSplit.length - 1];
                    return groupName;
                }));
        List<String> groupNameList = mapByGroupName.keySet().stream().sorted().collect(Collectors.toList());

        for(String groupName : groupNameList) {
            List<BiReportDownLoadParam> groupParams = mapByGroupName.get(groupName);
            // excel sheet名称最大长度31，超出31截取前31位
            String name = groupName;
            String sheetName = name.length() > 31 ? name.substring(0, 31) : name;
            excelWriter.setSheet(sheetName);
            // 数据写入
            writeData(excelWriter, groupParams);
        }
        // 剔除默认生成的第一个sheet
        excelWriter.getWorkbook().removeSheetAt(0);
    }

    /**
     * 写入数据
     *
     * @param writer writer
     * @param params 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    private void writeData(ExcelWriter writer, List<BiReportDownLoadParam> params) {
        List<String> regions = com.google.common.collect.Lists.newArrayList();
        int rowIndex = 0;
        int colIndex = 0;
        for (BiReportDownLoadParam param : params) {
            rowIndex = 0;
            List<String> xAxis = param.getXAxis();
            List<WrapDataVO> yAxis = param.getYAxis();
            String reportName = param.getReportName();

            // 写入报表名称
            writer.writeCellValue(colIndex, rowIndex, reportName);
            rowIndex++;
            // 写入X轴名称
            writer.writeCellValue(colIndex, rowIndex, param.getXAxisName());
            // 写X轴数据
            for (int i = 0; i < xAxis.size(); i++) {
                writer.writeCellValue(colIndex, rowIndex + i + 1, xAxis.get(i));
            }
            // 写入Y轴数据
            for (int i = 0; i < yAxis.size(); i++) {
                WrapDataVO yAxi = yAxis.get(i);
                List<String> yData = yAxi.getData();
                // 写入Y轴名称
                writer.writeCellValue(colIndex + i + 1, rowIndex, yAxi.getName());
                // 写入Y轴数据
                for (int j = 0; j < xAxis.size(); j++) {
                    String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "";
                    writer.writeCellValue(colIndex + i + 1, rowIndex + j + 1, value);
                }
            }
            // 添加空行 xAxis.size() + 1 为当前表格所占行数，再+1添加空行
            colIndex += yAxis.size() + 2;
        }
        DataBarUtil.addMinMaxDataBar(writer, regions);
        autoSizeColumnAll(writer);
    }

    public List<ReportStatisticTransferDetail> filter(List<ReportStatisticTransferDetail> reportDataList, String scoreField
            , String dimensionField, String dimensionValue, String itemName) {
        List<ReportStatisticTransferDetail> dataList = reportDataList.stream().filter(data -> {
            if(scoreField.equals(data.getScoreField()) && dimensionField.equals(data.getDimensionField())
                    && dimensionValue.equals(data.getDimensionValue()) && itemName.equals(data.getItemName())){
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return dataList;
    }

    public String sum(List<ReportStatisticTransferDetail> reportDataList, String itemName, Map<String, String> sumValueMap) {
        BigDecimal sumValue = reportDataList.stream().map((ReportStatisticTransferDetail data)-> {
            BigDecimal itemValueDecimal = new BigDecimal(String.valueOf(data.getItemValue()));
            return itemValueDecimal;
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        sumValueMap.put(itemName, sumValue.toString());
        return sumValue.toString();
    }

    public String sumRate(String dividendName, String divisorName, Map<String, String> sumValueMap) {
        String dividendSumValue = sumValueMap.get(dividendName);
        String divisorSumValue = sumValueMap.get(divisorName);
        BigDecimal dividendValue = new BigDecimal(dividendSumValue);
        BigDecimal divisorValue = new BigDecimal(divisorSumValue);
        if(BigDecimal.ZERO.compareTo(divisorValue)==0){
            return BigDecimal.ZERO.toString();
        }
        BigDecimal divideValue = dividendValue.divide(divisorValue, 6, RoundingMode.HALF_UP);
        return divideValue.toString();
    }

    public String calculateSum(String itemName, List<ReportStatisticTransferDetail> reportDataList, Map<String, String> sumValueMap) {
        switch (itemName){
            case "数据量":
            case "登录量":
            case "进件人数":
            case "批核人数":
            case "发起提现人数":
            case "放款成功人数":
                return sum(reportDataList, itemName, sumValueMap);
            case "评分分布":
                return sumRate("数据量", "数据量", sumValueMap);
            case "登录率":
                return sumRate("登录量", "数据量", sumValueMap);
            case "进件穿透率":
                return sumRate("进件人数", "数据量", sumValueMap);
            case "批核通过率":
                return sumRate("批核人数", "进件人数", sumValueMap);
            case "批核穿透率":
                return sumRate("批核人数", "数据量", sumValueMap);
            case "批核转化占比":
                return sumRate("批核人数", "批核人数", sumValueMap);
            case "发起提现率":
                return sumRate("发起提现人数", "批核人数", sumValueMap);
            case "放款成功率":
                return sumRate("放款成功人数", "发起提现人数", sumValueMap);
            case "放款成功穿透率":
                return sumRate("放款成功人数", "数据量", sumValueMap);
            case "放款成功转化占比":
                return sumRate("放款成功人数", "放款成功人数", sumValueMap);
        }
        return "0.00";
    }


    public Map<String, String> formatReportRules(String taskId) {
        ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(Long.valueOf(taskId));
        String reportRules = reportTask.getReportRules();

        Map<String, String> resultMap = new HashMap<>();
        if (reportRules.contains("upload")) {
            JSONObject param = JSONObject.parseObject(reportRules);
            String upload = param.getString("upload");
            JSONObject uploadJson = JSONObject.parseObject(upload);
            String dimensions = uploadJson.getString("dimensionsValue");

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 解析JSON数组为JsonNode
                JsonNode jsonNode = objectMapper.readTree(dimensions);
                resultMap = new HashMap<>();
                for (JsonNode item : jsonNode) {
                    String code = item.get("code").asText();
                    String desc = item.get("desc").asText();
                    resultMap.put(code, desc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public String getGroupName(String taskId, String groupCode) {
        Map<String, String> groupMap = formatReportRules(taskId);
        String groupName = groupMap.get(groupCode);
        if(StringUtils.isEmpty(groupName)){
            return "";
        }
        return groupName;
    }
}
