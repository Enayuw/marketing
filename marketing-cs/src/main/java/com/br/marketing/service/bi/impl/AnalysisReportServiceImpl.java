package com.br.marketing.service.bi.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.br.marketing.client.FastDfsClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.ReportStatisticsScore;
import com.br.marketing.entity.ReportStatisticsScoreExample;
import com.br.marketing.entity.ReportTask;
import com.br.marketing.entity.ScoreStatisticsDetail;
import com.br.marketing.entity.ScoreStatisticsDetailExample;
import com.br.marketing.mapper.ReportStatisticsScoreBaseMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ScoreStatisticsDetailBaseMapper;
import com.br.marketing.service.bi.AnalysisReportService;
import com.br.marketing.vo.bi.AxisWrapVo;
import com.br.marketing.vo.bi.WrapDataVo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AnalysisReportServiceImpl implements AnalysisReportService {

    public static final List<String> FIVE_STEP_LENGTH = Lists.newArrayList("[0,50)", "[50,100)", "[100,150)", "[150,200)", "[200,250)", "[250,300)",
        "[300,350)", "[350,400)", "[400,450)", "[450,500)", "[500,550)", "[550,600)", "[600,650)", "[650,700)", "[700,750)", "[750,800)", "[800,850)",
        "[850,900)", "[900,950)", "[950,1000]");

    public static final List<String> FIFTY_STEP_LENGTH =
        Lists.newArrayList("[0,5)", "[5,10)", "[10,15)", "[15,20)", "[20,25)", "[25,30)", "[30,35)", "[35,40)", "[40,45)", "[45,50)", "[50,55)",
            "[55,60)", "[60,65)", "[65,70)", "[70,75)", "[75,80)", "[80,85)", "[85,90)", "[90,95)", "[95,100]");

    public static final String BI_FILE_EXTENSION = ".xlsx";

    @Resource
    private FastDfsClient fastDfsClient;
    @Resource
    private ReportTaskMapper reportTaskMapper;
    @Resource
    private ReportStatisticsScoreBaseMapper reportStatisticsScoreBaseMapper;
    @Resource
    private ScoreStatisticsDetailBaseMapper scoreStatisticsDetailBaseMapper;

    @Override
    public String uploadReportToFastDfs(Long taskId) throws IOException {
        ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(taskId);
        List<AxisWrapVo> axisWrapVos = buildAxisWrapVo(taskId);
        log.warn("axisWrapVos:{}", axisWrapVos);
        // 分组sheet
        LinkedHashMap<String, AxisWrapVo> sheetMap = axisWrapVos.stream().collect(Collectors.toMap(axisWrapVo -> {
            String xAxisProduct = axisWrapVo.getXAxisProduct();
            String yAxisProduct = axisWrapVo.getYAxisProduct();
            return StringUtils.isEmpty(yAxisProduct) ? xAxisProduct : xAxisProduct + "_" + yAxisProduct;
        }, axisWrapVo -> axisWrapVo, (existing, replacement) -> existing, LinkedHashMap::new));
        ExcelWriter excelWriter = ExcelUtil.getWriter(true);
        String tempPath = Constants.TMP_FILE_PATH;
        // 保证每次生成目录不一样，后续根据目录删除临时文件时不会多删
        String uuid = IdUtil.simpleUUID();
        String tmpPath = tempPath + "/bi/" + uuid + File.separator;
        String fileName = reportTask.getReportName() + BI_FILE_EXTENSION;
        String fullName = tmpPath + FilenameUtils.getName(fileName);
        File tempFile = new File(fullName);
        boolean isFirstEntry = true;
        for (Map.Entry<String, AxisWrapVo> entry : sheetMap.entrySet()) {
            // excel sheet名称最大长度31，超出31截取前31位
            String sheetName = entry.getKey().length() > 31 ? entry.getKey().substring(0, 31) : entry.getKey();
            // 处理默认生成的sheet1
            if (isFirstEntry) {
                excelWriter.renameSheet(sheetName);
                isFirstEntry = false;
            } else {
                excelWriter.setSheet(sheetName);
            }
            writeDistributedData(excelWriter, entry.getValue());
        }
        // 写入文件并关闭流
        excelWriter.flush(tempFile);
        String url = fastDfsClient.uploadFile(tempFile);
        deleteTempFile(tmpPath);
        return url;
    }

    /**
     * 获取报告详细信息
     *
     * @param taskId 任务id
     * @return {@link List }<{@link AxisWrapVo }>
     * @author senyang.zheng
     * @date 2024/08/17
     */
    @Override
    public List<AxisWrapVo> getReportDetailsByTaskId(Long taskId) {
        return buildAxisWrapVo(taskId);
    }

    private List<AxisWrapVo> buildAxisWrapVo(Long taskId) {
        ReportStatisticsScoreExample statisticsExample = new ReportStatisticsScoreExample();
        statisticsExample.createCriteria().andIsDelEqualTo(1).andReportIdEqualTo(taskId);
        statisticsExample.setOrderByClause("statistics_order asc");
        List<ReportStatisticsScore> reportStatisticsScores = reportStatisticsScoreBaseMapper.selectByExample(statisticsExample);
        List<AxisWrapVo> axisWrapVos = Lists.newArrayList();
        for (ReportStatisticsScore statisticsScore : reportStatisticsScores) {
            AxisWrapVo axisWrapVo = new AxisWrapVo();
            axisWrapVo.setXAxisProduct(statisticsScore.getFieldX());
            axisWrapVo.setYAxisProduct(statisticsScore.getFieldY());
            ScoreStatisticsDetailExample detailExample = new ScoreStatisticsDetailExample();
            detailExample.createCriteria().andStatisticsIdEqualTo(statisticsScore.getId());
            List<ScoreStatisticsDetail> details = scoreStatisticsDetailBaseMapper.selectByExample(detailExample);
            switch (statisticsScore.getReportScoreType()) {
                case 1:
                    singleConvert(axisWrapVo, details);
                    break;
                case 2:
                    multipleConvert(axisWrapVo, details);
                    break;
                default:
                    break;
            }
            axisWrapVos.add(axisWrapVo);
        }
        return axisWrapVos;
    }

    private void multipleConvert(AxisWrapVo axisWrapVo, List<ScoreStatisticsDetail> details) {
        List<String> xAxis = determineStepLength(details, ScoreStatisticsDetail::getFieldXValue);
        List<String> yStep = determineStepLength(details, ScoreStatisticsDetail::getFieldYValue);
        // 按 field_y_value 和 field_x_value 分组
        Map<String, Map<String, Integer>> groupedByY = details.stream().collect(Collectors.groupingBy(ScoreStatisticsDetail::getFieldYValue,
            Collectors.toMap(ScoreStatisticsDetail::getFieldXValue, ScoreStatisticsDetail::getFieldNum)));
        // 构建 yAxis 列表
        List<WrapDataVo> yAxisData = yStep.stream().map(yValue -> {
            List<String> data =
                xAxis.stream().map(xValue -> String.valueOf(groupedByY.getOrDefault(yValue, Collections.emptyMap()).getOrDefault(xValue, 0)))
                    .collect(Collectors.toList());
            return new WrapDataVo(yValue, data);
        }).collect(Collectors.toList());
        axisWrapVo.setXAxis(xAxis);
        axisWrapVo.setYAxis(yAxisData);
    }

    private void singleConvert(AxisWrapVo axisWrapVo, List<ScoreStatisticsDetail> details) {
        // 根据数据确定使用哪种步长
        List<String> xAxis = determineStepLength(details, ScoreStatisticsDetail::getFieldXValue);
        // 按 field_y_value 分组
        Map<String, Map<String, Integer>> groupedByY = details.stream().collect(Collectors.groupingBy(ScoreStatisticsDetail::getFieldYValue,
            Collectors.toMap(ScoreStatisticsDetail::getFieldXValue, ScoreStatisticsDetail::getFieldNum)));
        // 构建 yAxis 列表
        List<String> keys = Splitter.on(",").splitToList(axisWrapVo.getXAxisProduct());
        List<WrapDataVo> yAxis = keys.stream().map(yName -> {
            // 根据 X轴步长 填充Y轴数据
            List<String> data =
                xAxis.stream().map(xValue -> String.valueOf(groupedByY.getOrDefault(yName, Collections.emptyMap()).getOrDefault(xValue, 0)))
                    .collect(Collectors.toList());
            return new WrapDataVo(yName, data);
        }).collect(Collectors.toList());
        // 设置横纵坐标轴的内容
        axisWrapVo.setXAxis(xAxis);
        axisWrapVo.setYAxis(yAxis);
    }

    private List<String> determineStepLength(List<ScoreStatisticsDetail> details, Function<ScoreStatisticsDetail, String> keyMapper) {
        Map<String, List<ScoreStatisticsDetail>> sectionData = details.stream().collect(Collectors.groupingBy(keyMapper));
        List<String> keys = Lists.newArrayList(sectionData.keySet());
        if (this.checkKeys(keys, FIVE_STEP_LENGTH)) {
            return FIVE_STEP_LENGTH;
        } else if (this.checkKeys(keys, FIFTY_STEP_LENGTH)) {
            return FIFTY_STEP_LENGTH;
        }
        return keys;
    }

    private boolean checkKeys(List<String> keys, List<String> config) {
        List<String> intersection = Lists.newArrayList(keys);
        intersection.retainAll(config);
        return CollectionUtil.isNotEmpty(intersection);
    }

    public void writeDistributedData(ExcelWriter writer, AxisWrapVo data) {
        List<String> xAxis = data.getXAxis();
        List<WrapDataVo> yAxis = data.getYAxis();
        // 写X轴数据
        writer.writeCellValue(0, 0,
            StringUtils.isEmpty(data.getYAxisProduct()) ? data.getXAxisProduct() : data.getXAxisProduct() + "\\" + data.getYAxisProduct());
        for (int i = 0; i < xAxis.size(); i++) {
            writer.writeCellValue(0, i + 1, xAxis.get(i));
        }
        // 写入Y轴数据
        for (int i = 0; i < yAxis.size(); i++) {
            WrapDataVo yAxi = yAxis.get(i);
            List<String> yData = yAxi.getData();
            // 按列写入
            writer.writeCellValue(i + 1, 0, yAxi.getName());
            // Write the Y-axis data
            for (int j = 0; j < xAxis.size(); j++) {
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "0";
                writer.writeCellValue(i + 1, j + 1, value);
            }
        }
        // 自适应宽度
        writer.autoSizeColumnAll();
    }

    public void deleteTempFile(String tmpPath) {
        // 删除指定目录
        Path directPath = Paths.get(tmpPath);
        // 删除目录下的所有 xlsx 文件，最后删除目录
        try (Stream<Path> paths = Files.walk(directPath)) {
            paths.sorted(Comparator.reverseOrder()).forEach((Path path) -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("Error deleting file: {}", e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            log.error("Error deleting files: {}", e.getMessage(), e);
        }
    }
}
