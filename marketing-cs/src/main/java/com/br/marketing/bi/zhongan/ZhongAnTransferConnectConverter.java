package com.br.marketing.bi.zhongan;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.zhongan.ReportStatisticField;
import com.br.marketing.dto.report.zhongan.ReportStatisticRule;
import com.br.marketing.dto.report.zhongan.ReportStatisticTransferDetail;
import com.br.marketing.entity.ReportFieldDict;
import com.br.marketing.entity.ReportFieldDictExample;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.ReportFieldDictMapper;
import com.br.marketing.mapper.ReportStatisticRuleMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.util.DataBarUtil;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ZhongAnTransferConnectConverter
 * @Description 转化分析报表
 * @Author LiXiang
 * @Date 2024-09-23
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.TRANSFER_CONNECT_REPORT)
public class ZhongAnTransferConnectConverter extends AbstractBiReportConverter<BiReportVO, ReportStatisticField> {

    @Resource
    private ZhongAnBiReportMapper zhongAnBiReportMapper;

    @Resource
    private ReportStatisticRuleMapper reportStatisticRuleMapper;

    @Resource
    private ReportFieldDictMapper reportFieldDictMapper;

    @Override
    public List<ReportStatisticField> fetchData(BiReportParam param) {
        return new ArrayList<>();
    }

    @Override
    public List<BiReportVO> process(List<ReportStatisticField> dataList, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();

        String reportType = ReportTaskTypeEnum.TRANSFER_CONNECT_TYPE.getValue().toString();
        String month = extend.getString("month");
        if(StringUtils.isEmpty(month)){
            month = DateUtil.format(LocalDateTime.now(), "yyyy-MM");
        }

        String queryMonthDay = month+"-01";
        LocalDate queryMonthLocalDate = LocalDate.parse(queryMonthDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        LocalDate curLocalDate = LocalDate.now();
        String curDate = curLocalDate.toString();
        LocalDate curMonthLocalDate = curLocalDate.withDayOfMonth(1);

        LocalDate queryLocalDateStart = queryMonthLocalDate.plusDays(1);
        LocalDate queryLocalDateEnd = queryMonthLocalDate.plusMonths(1).plusDays(1);
        if(queryMonthLocalDate.compareTo(curMonthLocalDate)==0){
            queryLocalDateEnd = curLocalDate.plusDays(1);
        }

        List<ReportStatisticRule> reportRuleList = reportStatisticRuleMapper.selectReportList(reportType
                , queryLocalDateStart.toString(), queryLocalDateEnd.toString());
        if (CollectionUtils.isEmpty(reportRuleList)) {
            List<BiReportVO> formatReportList = formatEmptyReport();
            return formatReportList;
        }

        // reportRuleListGroupByReportOrder
        Map<String, List<ReportStatisticRule>> reportRuleListGroupByReportOrder = reportRuleList.stream()
                .collect(Collectors.groupingBy(ReportStatisticRule::getReportOrder));
        List<String> reportOrderList = reportRuleListGroupByReportOrder.keySet().stream().sorted().collect(Collectors.toList());

        // reportRuleListGroupByReportDate
        Map<String, List<ReportStatisticRule>> reportRuleListGroupByReportDate = reportRuleList.stream()
                .collect(Collectors.groupingBy(ReportStatisticRule::getReportDate));
        List<String> reportDateList = reportRuleListGroupByReportDate.keySet().stream().sorted().collect(Collectors.toList());

        for (String reportOrder : reportOrderList) {
            BiReportVO biReportVO = new BiReportVO();
            biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_CONNECT_REPORT.getTypeName());
            // String reportName = getReportName(reportOrder);
            biReportVO.setReportName(BiReportTypeEnum.TRANSFER_CONNECT_REPORT.getStatName());
            biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
            biReportVO.setGroup(reportOrderToUserType(reportOrder));
            biReportVO.setXAxisName("日期");
            List<String> xAxis = new ArrayList<>();
            biReportVO.setXAxis(xAxis);

            for (String reportDate : reportDateList) {
                ReportStatisticRule reportRule = reportRuleList.stream()
                        .filter(rule -> reportOrder.equals(rule.getReportOrder()) && reportDate.equals(rule.getReportDate()))
                        .findFirst()
                        .orElse(null);
                if (reportRule == null) {
                    continue;
                }

                String reportId = reportRule.getReportId();

                List<String> fieldYTList = reportOrderToFieldYList(reportOrder, reportDate, "T");
                List<ReportStatisticField> reportDateTList = zhongAnBiReportMapper.queryReportStatisticFieldbI_(reportId, fieldYTList, "reportDate", "");
                if (CollectionUtils.isEmpty(reportDateTList)) {
                    continue;
                }

                List<String> reportDateTValueList = reportDateTList.stream().map((data -> data.getItemValue())).collect(Collectors.toList());

                // 构造横坐标数据
                xAxis.addAll(reportDateTValueList);

                // 构建M
                if (queryLocalDateEnd.minusDays(1).toString().equals(reportDate)) {
                    List<String> fieldYMList = reportOrderToFieldYList(reportOrder, reportDate, "M");
                    List<ReportStatisticField> reportDateMList = zhongAnBiReportMapper.queryReportStatisticFieldbI_(reportId, fieldYMList, "reportDate", "");
                    List<String> reportDateMValueList = reportDateMList.stream().map((data -> data.getItemValue())).collect(Collectors.toList());
                    xAxis.addAll(reportDateMValueList);
                }
            }

            ReportFieldDictExample reportFieldDictExample = new ReportFieldDictExample();
            reportFieldDictExample.createCriteria().andReportTypeEqualTo("17").andUserTypeEqualTo(reportOrderToUserType(reportOrder));
            reportFieldDictExample.setOrderByClause("item_order asc");
            List<ReportFieldDict> reportFieldDictList = reportFieldDictMapper.selectByExample(reportFieldDictExample);

            List<WrapDataVO> yAxis = Lists.newArrayList();
            for (ReportFieldDict reportFieldDict : reportFieldDictList) {
                String itemName = reportFieldDict.getItemName();
                String itemShow = reportFieldDict.getItemShow();
                String formatTypeName = reportFieldDict.getItemFormatType();
                FormatType formatType = FormatType.getByName(formatTypeName);
                // 构造纵坐标数据
                List<ReportStatisticField> reportFieldList = new ArrayList<>();

                for (String reportDate : reportDateList) {
                    ReportStatisticRule reportRule = reportRuleList.stream()
                            .filter(rule -> reportOrder.equals(rule.getReportOrder()) && reportDate.equals(rule.getReportDate()))
                            .findFirst()
                            .orElse(null);
                    if (reportRule == null) {
                        continue;
                    }
                    String reportId = reportRule.getReportId();

                    List<String> fieldYTList = reportOrderToFieldYList(reportOrder, reportDate, "T");
                    List<ReportStatisticField> reportFieldTList = zhongAnBiReportMapper.queryReportStatisticFieldbI_(reportId, fieldYTList, itemName, "");
                    reportFieldList.addAll(reportFieldTList);
                    if (queryLocalDateEnd.minusDays(1).toString().equals(reportDate)) {
                        List<String> fieldYMList = reportOrderToFieldYList(reportOrder, reportDate, "M");
                        List<ReportStatisticField> reportFieldMList = zhongAnBiReportMapper.queryReportStatisticFieldbI_(reportId, fieldYMList, itemName, "");
                        reportFieldList.addAll(reportFieldMList);
                    }
                }
                yAxis.add(buildWrapDataVO(itemShow, reportFieldList, ReportStatisticField::getItemValue, formatType));
            }
            biReportVO.setYAxis(yAxis);
            biReportVOList.add(biReportVO);
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
     * @param params      参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    @Override
    public void exportData(ExcelWriter excelWriter, List<BiReportDownLoadParam> params) {
        for (BiReportDownLoadParam param : params) {
            String name = "场景" + param.getGroup()+ param.getReportName();
            // excel sheet名称最大长度31，超出31截取前31位
            String sheetName = name.length() > 31 ? name.substring(0, 31) : name;
            excelWriter.setSheet(sheetName);
            // 数据写入
            List<BiReportDownLoadParam> list = Lists.newArrayList();
            param.setReportName(BiReportTypeEnum.TRANSFER_CONNECT_REPORT.getStatName());
            list.add(param);
            writeData(excelWriter, list);
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
        int rowIndex = 0;
        List<String> regions = com.google.common.collect.Lists.newArrayList();
        for (BiReportDownLoadParam param : params) {
            List<String> xAxis = param.getXAxis();
            List<WrapDataVO> yAxis = param.getYAxis();
            // String reportName = param.getReportName();

            // 写入报表名称
            // writer.writeCellValue(0, rowIndex, reportName);
            // rowIndex++;
            // 写入X轴名称
            writer.writeCellValue(0, rowIndex, param.getXAxisName());
            // 写X轴数据
            for (int i = 0; i < xAxis.size(); i++) {
                writer.writeCellValue(0, rowIndex + i + 1, xAxis.get(i));
            }
            // 写入Y轴数据
            for (int i = 0; i < yAxis.size(); i++) {
                WrapDataVO yAxi = yAxis.get(i);
                List<String> yData = yAxi.getData();
                // 写入Y轴名称
                writer.writeCellValue(i + 1, rowIndex, yAxi.getName());
                // 写入Y轴数据
                for (int j = 0; j < xAxis.size(); j++) {
                    String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "";
                    writer.writeCellValue(i + 1, rowIndex + j + 1, value);
                }
            }
            // 添加空行 xAxis.size() + 1 为当前表格所占行数，再+1添加空行
            rowIndex += xAxis.size() + 2;
        }
        DataBarUtil.addMinMaxDataBar(writer, regions);
        autoSizeColumnAll(writer);
    }

    public List<BiReportVO> formatEmptyReport(){
        List<BiReportVO> biReportVOList = Lists.newArrayList();

        List<String> reportOrderList = new ArrayList<>();
        reportOrderList.add("1");
        reportOrderList.add("2");
        reportOrderList.add("3");
        for (String reportOrder : reportOrderList) {
            BiReportVO biReportVO = new BiReportVO();
            biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_CONNECT_REPORT.getTypeName());
            // String reportName = getReportName(reportOrder);
            biReportVO.setReportName(BiReportTypeEnum.TRANSFER_CONNECT_REPORT.getStatName());
            biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
            biReportVO.setXAxisName("日期");
            biReportVO.setGroup(reportOrderToUserType(reportOrder));
            List<String> xAxis = new ArrayList<>();
            biReportVO.setXAxis(xAxis);

            ReportFieldDictExample reportFieldDictExample = new ReportFieldDictExample();
            reportFieldDictExample.createCriteria().andReportTypeEqualTo("17").andUserTypeEqualTo(reportOrderToUserType(reportOrder));
            reportFieldDictExample.setOrderByClause("item_order asc");
            List<ReportFieldDict> reportFieldDictList = reportFieldDictMapper.selectByExample(reportFieldDictExample);

            List<WrapDataVO> yAxis = Lists.newArrayList();
            for (ReportFieldDict reportFieldDict : reportFieldDictList) {
                // String itemName = reportFieldDict.getItemName();
                String itemShow = reportFieldDict.getItemShow();
                String formatTypeName = reportFieldDict.getItemFormatType();
                FormatType formatType = FormatType.getByName(formatTypeName);
                // 构造纵坐标数据
                List<ReportStatisticField> reportFieldList = new ArrayList<>();
                yAxis.add(buildWrapDataVO(itemShow, reportFieldList, ReportStatisticField::getItemValue, formatType));
            }
            biReportVO.setYAxis(yAxis);
            biReportVOList.add(biReportVO);
        }
        return biReportVOList;
    }

    public List<ReportStatisticTransferDetail> filter(List<ReportStatisticTransferDetail> reportDataList, String scoreField
            , String dimensionField, String dimensionValue, String itemName) {
        List<ReportStatisticTransferDetail> dataList = reportDataList.stream().filter(data -> {
            if (scoreField.equals(data.getScoreField()) && dimensionField.equals(data.getDimensionField())
                    && dimensionValue.equals(data.getDimensionValue()) && itemName.equals(data.getItemName())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return dataList;
    }

    public String getReportName(String reportOrder) {
        String reportName = "";
        switch (reportOrder) {
            case "1":
                reportName = "场景1";
                break;
            case "2":
                reportName = "场景7";
                break;
            case "3":
                reportName = "场景8";
                break;
        }
        return reportName;
    }

    public String reportOrderToUserType(String reportOrder) {
        String userType = "";
        switch (reportOrder) {
            case "1":
                userType = "1";
                break;
            case "2":
                userType = "7";
                break;
            case "3":
                userType = "8";
                break;
        }
        return userType;
    }

    public List<String> reportOrderToFieldYList(String reportOrder, String reportDate, String type) {
        List<String> fieldYList = new ArrayList<>();
        String fieldDate = LocalDate.parse(reportDate).minusDays(1).toString();
        if ("1".equals(reportOrder)) {
            fieldYList.add(fieldDate + "_" + type + "_首登");
            fieldYList.add(fieldDate + "_" + type + "_非首登");
        } else {
            fieldYList.add(fieldDate + "_" + type);
        }
        return fieldYList;
    }

    public static void main(String[] args) {
        String dateString = "2024-11";

        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        try {
            // 解析字符串为 LocalDate，默认设置为该月的第一天
            LocalDate date = LocalDate.parse(dateString, formatter);
            System.out.println("Parsed LocalDate: " + date);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format: " + e.getMessage());
        }
    }
}
