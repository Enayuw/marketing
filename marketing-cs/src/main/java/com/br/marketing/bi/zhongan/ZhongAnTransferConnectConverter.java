package com.br.marketing.bi.zhongan;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.zhongan.ReportStatisticField;
import com.br.marketing.dto.report.zhongan.ReportStatisticRule;
import com.br.marketing.dto.report.zhongan.ReportStatisticTransferDetail;
import com.br.marketing.entity.ReportFieldMapping;
import com.br.marketing.entity.ReportFieldMappingExample;
import com.br.marketing.entity.ReportTask;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportFieldMappingMapper;
import com.br.marketing.mapper.ReportStatisticRuleMapper;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
    private ReportFieldMappingMapper reportFieldMappingMapper;

    @Resource
    private ReportTaskMapper reportTaskMapper;

    @Override
    public List<ReportStatisticField> fetchData(BiReportParam param) {
        return new ArrayList<>();
    }

    @Override
    public List<BiReportVO> process(List<ReportStatisticField> dataList, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();

        String taskId = extend.getString("taskId");
        LocalDate curLocalDate = LocalDate.now();
        String reportDateStart = curLocalDate.withDayOfMonth(1).toString();
        String reportDateEnd = curLocalDate.plusDays(1).toString();

        List<ReportStatisticRule> reportRuleList = reportStatisticRuleMapper.selectReportList(taskId, reportDateStart, reportDateEnd);
        if (CollectionUtils.isEmpty(reportRuleList)) {
            return new ArrayList<>();
        }

        // reportRuleListGroupByReportOrder
        Map<String, List<ReportStatisticRule>> reportRuleListGroupByReportOrder = reportRuleList.stream()
                .collect(Collectors.groupingBy(ReportStatisticRule::getReportOrder));
        List<String> ReportOrderList = reportRuleListGroupByReportOrder.keySet().stream().sorted().collect(Collectors.toList());

        // reportRuleListGroupByReportDate
        Map<String, List<ReportStatisticRule>> reportRuleListGroupByReportDate = reportRuleList.stream()
                .collect(Collectors.groupingBy(ReportStatisticRule::getReportDate));
        List<String> ReportDateList = reportRuleListGroupByReportDate.keySet().stream().sorted().collect(Collectors.toList());

        ReportFieldMappingExample reportFieldDictExample = new ReportFieldMappingExample();
        reportFieldDictExample.createCriteria().andReportTaskIdEqualTo(taskId);
        reportFieldDictExample.setOrderByClause("item_order asc");
        List<ReportFieldMapping> reportFieldMappingList = reportFieldMappingMapper.selectByExample(reportFieldDictExample);

        for(String reportOrder : ReportOrderList){
            BiReportVO biReportVO = new BiReportVO();
            biReportVO.setReportTypeName(BiReportTypeEnum.TRANSFER_CONNECT_REPORT.getTypeName());
            String groupName = getGroupName(taskId, "");
            biReportVO.setReportName(BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT.getStatName()+"-"+"12345"+"-"+groupName);
            biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
            biReportVO.setXAxisName("日期");
            List<String> xAxis = new ArrayList<>();
            biReportVO.setXAxis(xAxis);

            for(String reportDate : ReportDateList){
                ReportStatisticRule reportRule = reportRuleList.stream()
                        .filter(rule -> reportOrder.equals(rule.getReportOrder()) && reportDate.equals(rule.getReportDate()))
                        .findFirst()
                        .orElse(null);
                if(reportRule == null){
                    continue;
                }

                String reportId = reportRule.getReportId();
                List<ReportStatisticField> reportDateList = zhongAnBiReportMapper.queryReportStatisticFieldbI_(reportId, "reportDate");
                String reportDateValue = reportDateList.get(0).getItemValue();

                // 构造横坐标数据
                xAxis.add(reportDateValue);

                // 构造纵坐标数据
                List<WrapDataVO> yAxis = Lists.newArrayList();

                for(ReportFieldMapping reportFieldMapping: reportFieldMappingList){
                    String itemName = reportFieldMapping.getItemName();
                    String itemShow = reportFieldMapping.getItemShow();
                    String formatTypeName = reportFieldMapping.getItemFormatType();
                    FormatType formatType = FormatType.getByName(formatTypeName);
                    List<ReportStatisticField> reportFieldList = zhongAnBiReportMapper.queryReportStatisticFieldbI_(reportId, itemName);

                    // sort
                    yAxis.add(buildWrapDataVO(itemShow, reportFieldList, ReportStatisticField::getItemValue, formatType));
                }
                biReportVO.setYAxis(yAxis);
            }
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
     * @param params 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    @Override
    public void exportData(ExcelWriter excelWriter, List<BiReportDownLoadParam> params) {
        // excel sheet名称最大长度31，超出31截取前31位
        String name = params.get(0).getReportName();
        String sheetName = name.length() > 31 ? name.substring(0, 31) : name;
        excelWriter.setSheet(sheetName);
        // 数据写入
        writeData(excelWriter, params);
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
            String reportName = param.getReportName();

            // 写入报表名称
            writer.writeCellValue(0, rowIndex, reportName);
            rowIndex++;
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
