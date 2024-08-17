package com.br.marketing.service.bi.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        LinkedHashMap<String, AxisWrapVo> map = axisWrapVos.stream().collect(Collectors.toMap(axisWrapVo -> {
            String xAxisProduct = axisWrapVo.getXAxisProduct();
            String yAxisProduct = axisWrapVo.getYAxisProduct();
            return StringUtils.isEmpty(yAxisProduct) ? xAxisProduct : xAxisProduct + "_" + yAxisProduct;
        }, axisWrapVo -> axisWrapVo, (existing, replacement) -> existing, LinkedHashMap::new));
        ExcelWriter excelWriter = ExcelUtil.getWriter(true);
        String tempPath = Constants.TMP_FILE_PATH;
        // 保证每次生成目录不一样，后续根据目录删除临时文件时不会多删
        String uuid = IdUtil.simpleUUID();
        String tmpPath = tempPath + "/bi/" + uuid + File.separator;
        String fileName = reportTask.getReportName();
        String fullName = tmpPath + FilenameUtils.getName(fileName);
        File tempFile = new File(fullName);
        map.forEach((key, value) -> {
            excelWriter.setSheet(key);
            writeDistributedData(excelWriter, value);
        });
        excelWriter.flush(tempFile);
        String url = fastDfsClient.uploadFile(tempFile);
        // deleteTempFile(tmpPath);
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
            // 获取数据中全量区间段
            List<String> sections = details.stream().map(ScoreStatisticsDetail::getFieldXValue).distinct()
                .sorted(Comparator.comparing(String::valueOf, Comparator.reverseOrder())).collect(Collectors.toList());
            // 根据X轴字段分组
            Map<String, List<ScoreStatisticsDetail>> sectionData =
                details.stream().collect(Collectors.groupingBy(ScoreStatisticsDetail::getFieldXValue));
            // 填充空分段数据
            fillSectionData(sectionData);
            List<WrapDataVo> wrapDataVoList = sectionData.entrySet().stream().map((Map.Entry<String, List<ScoreStatisticsDetail>> entry) -> {
                List<ScoreStatisticsDetail> detailList = entry.getValue();
                List<ScoreStatisticsDetail> missingStatisDates = sections.stream()
                    .filter(
                        (String section) -> detailList.stream().noneMatch((ScoreStatisticsDetail detail) -> detail.getFieldXValue().equals(section)))
                    .map((String section) -> {
                        ScoreStatisticsDetail defaultDetail = new ScoreStatisticsDetail();
                        defaultDetail.setFieldXValue(section);
                        defaultDetail.setFieldNum(0);
                        return defaultDetail;
                    }).collect(Collectors.toList());
                detailList.addAll(missingStatisDates);
                detailList.sort(Comparator.comparing(ScoreStatisticsDetail::getFieldXValue, Comparator.reverseOrder()));
                List<String> numList = detailList.stream().map(detail -> String.valueOf(detail.getFieldNum())).collect(Collectors.toList());
                return new WrapDataVo(entry.getKey(), numList);
            }).collect(Collectors.toList());
            axisWrapVo.setXAxis(sections);
            axisWrapVo.setYAxis(wrapDataVoList);
            axisWrapVos.add(axisWrapVo);
        }
        return axisWrapVos;
    }

    protected void fillSectionData(Map<String, List<ScoreStatisticsDetail>> sectionData) {
        List<String> keys = Lists.newArrayList(sectionData.keySet());
        if (this.checkKeys(keys, FIVE_STEP_LENGTH)) {
            List<String> difference = Lists.newArrayList(FIVE_STEP_LENGTH);
            difference.removeAll(keys);
            difference.forEach(key -> sectionData.put(key, Lists.newArrayList()));
        } else if (this.checkKeys(keys, FIFTY_STEP_LENGTH)) {
            List<String> difference = Lists.newArrayList(FIFTY_STEP_LENGTH);
            difference.removeAll(keys);
            difference.forEach(key -> sectionData.put(key, Lists.newArrayList()));
        }
    }

    private boolean checkKeys(List<String> keys, List<String> config) {
        List<String> intersection = Lists.newArrayList(keys);
        intersection.retainAll(config);
        return CollectionUtil.isNotEmpty(intersection);
    }

    public void writeDistributedData(ExcelWriter writer, AxisWrapVo data) {
        int rowIndex = 0;
        writer.writeCellValue(0, rowIndex, data.getXAxisProduct() + "\\" + data.getYAxisProduct());
        List<String> xAxis = data.getXAxis();
        for (int i = 0; i < xAxis.size(); i++) {
            writer.writeCellValue(i + 1, rowIndex, xAxis.get(i));
        }
        rowIndex++;
        List<WrapDataVo> yAxis = data.getYAxis();
        for (WrapDataVo yAxi : yAxis) {
            writer.writeCellValue(0, rowIndex, yAxi.getName());
            List<String> yData = yAxi.getData();
            for (int j = 1; j <= yData.size(); j++) {
                writer.writeCellValue(j, rowIndex, StringUtils.isNotEmpty(yData.get(j - 1)) ? yData.get(j - 1) : "0");
            }
            rowIndex++;
        }
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

    private List<AxisWrapVo> extracted() {
        List<AxisWrapVo> list = Lists.newArrayList();
        AxisWrapVo axisWrapVo = new AxisWrapVo();
        axisWrapVo.setXAxisProduct("scoreA");
        axisWrapVo.setYAxisProduct("scoreB");
        // axisWrapVo.setTitle("测试评分分布1");
        // axisWrapVo.setIsPercent(0);
        // axisWrapVo.setType("line");
        axisWrapVo.setXAxis(Lists.newArrayList("[0,50)", "[50,100)", "[100,150)", "[150,200)", "[200,250)", "[250,300)", "[300,350)", "[350,400)",
            "[400,450)", "[450,500)", "[500,550)", "[550,600)", "[600,650)", "[650,700)", "[700,750)", "[750,800)", "[800,850)", "[850,900)",
            "[900,950)", "[950,1000]"));
        axisWrapVo.setYAxis(getWrapDataVo());
        list.add(axisWrapVo);
        return list;
    }

    private List<WrapDataVo> getWrapDataVo() {
        List<WrapDataVo> yAxis = Lists.newArrayList();
        List<String> keys = Lists.newArrayList("[0,50)", "[50,100)", "[100,150)", "[150,200)", "[200,250)", "[250,300)", "[300,350)", "[350,400)",
            "[400,450)", "[450,500)", "[500,550)", "[550,600)", "[600,650)", "[650,700)", "[700,750)", "[750,800)", "[800,850)", "[850,900)",
            "[900,950)", "[950,1000]");
        int len = keys.size();
        int dataNum = 0;
        for (String key : keys) {
            WrapDataVo wrapDataVo = new WrapDataVo();
            wrapDataVo.setName(key);
            List<String> dataList = Lists.newArrayList();
            for (int i = 0; i < len; i++) {
                dataList.add(String.valueOf(++dataNum));
            }
            wrapDataVo.setData(dataList);
            yAxis.add(wrapDataVo);
        }
        return yAxis;
    }
}
