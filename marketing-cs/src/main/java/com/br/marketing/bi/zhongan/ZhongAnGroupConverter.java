package com.br.marketing.bi.zhongan;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.ScoreFieldDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnDistributionStatisticDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnGroupedScoreDistributionDTO;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import com.br.marketing.entity.ReportTask;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.DataBarUtil;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.GROUP_SCORE_REPORT)
public class ZhongAnGroupConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnGroupedScoreDistributionDTO> {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;
    @Autowired
    ReportTaskMapper reportTaskMapper;
    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public List<ZhongAnGroupedScoreDistributionDTO> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");
        if (StringUtils.isEmpty(taskId)) {
            return new ArrayList<>();
        }
        // 查询规则分组名称
        ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(Long.valueOf(taskId));
        Map<String, String> groupNameMap = formatReportRules(reportTask);

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
        List<ScoreFieldDTO> scoreFieldDTOS = JSON.parseObject(scoreField, new TypeReference<List<ScoreFieldDTO>>() {
        }.getType());
        List<ZhongAnGroupedScoreDistributionDTO> dtos = new ArrayList<>();
        for (ScoreFieldDTO scoreFieldDTO : scoreFieldDTOS) {
            String dimensionField = reportStatisticTransfer.getDimensionField();
            String dimensionValue = reportStatisticTransfer.getDimensionValue();
            String field = scoreFieldDTO.getField();
            Integer step = scoreFieldDTO.getStep();
            // 分组查询
            for (String value : formatField(dimensionValue)) {
                List<ZhongAnDistributionStatisticDTO> dtoList = zhongAnBiReportMapper.
                        selectZaMultiHeadGroupListbI_(reportId, field, dimensionField, value, "案件量");
                bulidGroupHead(dtoList, field, groupNameMap.get(value), step, dtos);
            }
        }
        return dtos;
    }

    private void bulidGroupHead(List<ZhongAnDistributionStatisticDTO> dtoList, String field, String groupName,
                                Integer step, List<ZhongAnGroupedScoreDistributionDTO> dtos) {
        List<ZhongAnGroupedScoreDistributionDTO> list = new ArrayList<>();
        for (ZhongAnDistributionStatisticDTO zhongAnDistributionStatisticDTO : dtoList) {
            ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
            zhongAnGroupedScoreDistributionDTO.setProduct(field);
            zhongAnGroupedScoreDistributionDTO.setInterval(zhongAnDistributionStatisticDTO.getScoreValue());
            zhongAnGroupedScoreDistributionDTO.setGroup(groupName == null ? "0":groupName);
            zhongAnGroupedScoreDistributionDTO.setName(zhongAnDistributionStatisticDTO.getItemName());
            zhongAnGroupedScoreDistributionDTO.setNum(Long.valueOf(zhongAnDistributionStatisticDTO.getItemValue()));
            zhongAnGroupedScoreDistributionDTO.setStep(step);
            list.add(zhongAnGroupedScoreDistributionDTO);
        }
        fillProportion(list,ZhongAnGroupedScoreDistributionDTO::getNum,ZhongAnGroupedScoreDistributionDTO::setProportion);
        dtos.addAll(list);
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnGroupedScoreDistributionDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOS = Lists.newArrayList();

        Map<String, Map<String, List<ZhongAnGroupedScoreDistributionDTO>>> groupedByProductAndGroupName =
                dtos.stream().collect(Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getProduct,
                        Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getGroup)
                ));

        List<String> fiftyStepLengthList = marketingCommonConfig.getBiReportStepConfig().get("fiftyStepLength");
        List<String> fiveStepLengthList = marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");

        for (Map.Entry<String, Map<String, List<ZhongAnGroupedScoreDistributionDTO>>> entry1 : groupedByProductAndGroupName.entrySet()) {
            for (Map.Entry<String, List<ZhongAnGroupedScoreDistributionDTO>> entry : entry1.getValue().entrySet()) {
                // Y轴数据
                List<WrapDataVO> yAxisData = Lists.newArrayList();
                Map<String, List<ZhongAnGroupedScoreDistributionDTO>> comparisonMap = entry.getValue().stream()
                        .collect(Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getName));

                // X轴数据
                List<String> intervals = new ArrayList<>();
                Integer step;
                String group = "";
                String reportTaskName = "";
                // 处理数据
                for (String comparisonName : comparisonMap.keySet()) {
                    List<ZhongAnGroupedScoreDistributionDTO> comparisonData = comparisonMap.get(comparisonName);
                    step = comparisonData.get(0).getStep();
                    intervals.clear();
                    if(step == 50){
                        intervals.addAll(fiftyStepLengthList);
                    }else {
                        intervals.addAll(fiveStepLengthList);
                    }
                    group = comparisonData.get(0).getGroup();
                    List<ZhongAnGroupedScoreDistributionDTO> list = new ArrayList<>();
                    Long sum = 0L;
                    for (String interval : intervals){
                        int size = comparisonData.size();
                        int num = 1;
                        for (ZhongAnGroupedScoreDistributionDTO dto : comparisonData) {
                            if(interval.equals(dto.getInterval())){
                                sum += dto.getNum();
                                list.add(dto);
                                break;
                            }else if(size == num){
                                ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
                                zhongAnGroupedScoreDistributionDTO.setProduct(dto.getProduct());
                                zhongAnGroupedScoreDistributionDTO.setInterval(interval);
                                zhongAnGroupedScoreDistributionDTO.setName(dto.getName());
                                zhongAnGroupedScoreDistributionDTO.setNum(0L);
                                zhongAnGroupedScoreDistributionDTO.setProportion(BigDecimal.ZERO);
                                zhongAnGroupedScoreDistributionDTO.setStep(dto.getStep());
                                list.add(zhongAnGroupedScoreDistributionDTO);
                            }else {
                                num ++;
                            }
                        }
                    }
                    // 占比字段格式化
                    List<ZhongAnGroupedScoreDistributionDTO> transformedList = list.stream()
                            .map((ZhongAnGroupedScoreDistributionDTO dto) -> {
                                BigDecimal newProportion = dto.getProportion().multiply(BigDecimal.valueOf(100));
                                dto.setProportion(newProportion.setScale(2, RoundingMode.HALF_UP));
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // 增加总计
                    ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
                    zhongAnGroupedScoreDistributionDTO.setNum(sum);
                    zhongAnGroupedScoreDistributionDTO.setProportion(BigDecimal.valueOf(100));
                    transformedList.add(zhongAnGroupedScoreDistributionDTO);

                    yAxisData.add(buildWrapDataVO(comparisonName + "量级", transformedList, ZhongAnGroupedScoreDistributionDTO::getNum,
                            FormatType.THOUSAND_SEPARATOR));
                    yAxisData.add(buildWrapDataVO(comparisonName + "占比", transformedList, ZhongAnGroupedScoreDistributionDTO::getProportion,
                            FormatType.PERCENT_SIGN));
                }
                BiReportVO biReportVO = new BiReportVO();
                biReportVO.setReportName(entry1.getKey());
                biReportVO.setReportTypeName(BiReportTypeEnum.GROUP_SCORE_REPORT.getTypeName());
                biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
                biReportVO.setXAxisName("区间");
                biReportVO.setGroup(group);
                if(!intervals.contains("总计")){
                    intervals.add("总计");
                }
                biReportVO.setXAxis(intervals);
                biReportVO.setYAxis(yAxisData);
                biReportVOS.add(biReportVO);
            }
        }
        return biReportVOS;
    }


    public Map<String, String> formatReportRules(ReportTask reportTask) {
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


    public List<String> formatField(String str) {
        return JSON.parseObject(str, new TypeReference<List<String>>() {
        }.getType());
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
        // excel sheet名称最大长度31，超出31截取前31位
        String name = params.get(0).getReportName() + "_" + params.get(0).getGroup();
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
        int startRow = 0;
        List<String> regions = Lists.newArrayList();
        for (BiReportDownLoadParam param : params) {
            List<String> xAxis = param.getXAxis();
            List<WrapDataVO> yAxis = param.getYAxis();
            // 写入X轴名称
            writer.writeCellValue(0, rowIndex, param.getReportName() + "_" + param.getGroup());
            rowIndex++;
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
                    if (value.contains("%")) {
                        BigDecimal decimal = new BigDecimal(value.replace("%", ""));
                        writer.writeCellValue(i + 1, rowIndex + j + 1, decimal.divide(BigDecimal.valueOf(100),
                                decimal.scale() + 2, RoundingMode.HALF_UP));
                        writer.getCell(i + 1, rowIndex + j + 1).setCellStyle(getDataBarCellStyle(writer, decimal));
                    } else {
                        writer.writeCellValue(i + 1, rowIndex + j + 1, value);
                    }
                }
                if (i % 2 == 1) {
                    String startCell = CellReference.convertNumToColString(i + 1) + (startRow + 2);
                    String endCell = CellReference.convertNumToColString(i + 1) + (startRow + xAxis.size());
                    String region = startCell + ":" + endCell;
                    regions.add(region);
                }
            }
            // 添加空行 xAxis.size() + 1 为当前表格所占行数，再+1添加空行
            rowIndex += xAxis.size() + 2;
            startRow = rowIndex;
        }
        DataBarUtil.addMinMaxDataBar(writer, regions);
        autoSizeColumnAll(writer);
    }
}
