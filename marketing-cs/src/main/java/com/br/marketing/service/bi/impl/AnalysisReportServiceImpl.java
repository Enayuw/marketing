package com.br.marketing.service.bi.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.br.common.log.AlertLog;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
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
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.DataBarUtil;
import com.br.marketing.vo.bi.AxisWrapVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.style.StyleUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AnalysisReportServiceImpl implements AnalysisReportService {

    public static final String BI_FILE_EXTENSION = ".xlsx";

    @Resource
    private FastDfsClient fastDfsClient;
    @Resource
    private ReportTaskMapper reportTaskMapper;
    @Resource
    private ReportStatisticsScoreBaseMapper reportStatisticsScoreBaseMapper;
    @Resource
    private ScoreStatisticsDetailBaseMapper scoreStatisticsDetailBaseMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadReportToFastDfs(Long taskId) throws IOException {
        log.warn("上传taskId:{}任务报表", taskId);
        ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(taskId);
        reportTask.setDownStatus(1);
        reportTaskMapper.updateByPrimaryKeySelective(reportTask);
        List<AxisWrapVO> axisWrapVOS = buildAxisWrapVo(taskId);
        // 分组sheet
        LinkedHashMap<String, AxisWrapVO> sheetMap = axisWrapVOS.stream().collect(Collectors.toMap((AxisWrapVO axisWrapVO) -> {
            String xAxisProduct = axisWrapVO.getXAxisProduct();
            String yAxisProduct = axisWrapVO.getYAxisProduct();
            return StringUtils.isEmpty(yAxisProduct) ? xAxisProduct : (xAxisProduct + "_" + yAxisProduct);
        }, axisWrapVO -> axisWrapVO, (existing, replacement) -> existing, LinkedHashMap::new));
        ExcelWriter excelWriter = ExcelUtil.getWriter(true);
        String tempPath = Constants.TMP_FILE_PATH;
        // 保证每次生成目录不一样，后续根据目录删除临时文件时不会多删
        String uuid = IdUtil.simpleUUID();
        String tmpPath = tempPath + "/bi/" + uuid + File.separator;
        String fileName = reportTask.getReportName() + BI_FILE_EXTENSION;
        String fullName = tmpPath + FilenameUtils.getName(fileName);
        File tempFile = new File(fullName);
        Set<String> sheetNames = new HashSet<>();
        int counter = 1;
        for (Map.Entry<String, AxisWrapVO> entry : sheetMap.entrySet()) {
            // excel sheet名称最大长度31，超出31截取前31位
            String sheetName = entry.getKey().length() > 28 ? entry.getKey().substring(0, 28) : entry.getKey();
            String originalSheetName = sheetName;
            if (sheetNames.contains(sheetName)) {
                sheetName = String.format("%s_%s", originalSheetName, counter++);
            }
            sheetNames.add(originalSheetName);
            excelWriter.setSheet(sheetName);
            if (entry.getValue().getStatisticsDesc() == null) {
                writeDistributedData(excelWriter, entry.getValue());
            } else {
                writeErrorData(excelWriter, entry.getValue());
            }
        }
        // 剔除默认生成的第一个sheet，写入文件并关闭流
        excelWriter.getWorkbook().removeSheetAt(0);
        excelWriter.flush(tempFile);
        String url;
        try {
            url = fastDfsClient.uploadFile(tempFile);
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "fastDfs 上传文件异常 手动重试一次"));
            url = fastDfsClient.uploadFile(tempFile);
        }
        deleteTempFile(tmpPath);
        reportTask.setDownStatus(2);
        reportTask.setDownloadUrl(url);
        reportTaskMapper.updateByPrimaryKeySelective(reportTask);
        return url;
    }

    private void writeErrorData(ExcelWriter writer, AxisWrapVO wrapVO) {
        writer.writeCellValue(0, 0, wrapVO.getStatisticsDesc());
        writer.autoSizeColumnAll();
    }

    /**
     * 获取报告详细信息
     *
     * @param taskId 任务id
     * @return {@link List }<{@link AxisWrapVO }>
     * @author senyang.zheng
     * @date 2024/08/17
     */
    @Override
    public List<AxisWrapVO> getReportDetailsByTaskId(Long taskId) {
        return buildAxisWrapVo(taskId);
    }

    private List<AxisWrapVO> buildAxisWrapVo(Long taskId) {
        ReportStatisticsScoreExample statisticsExample = new ReportStatisticsScoreExample();
        statisticsExample.createCriteria().andIsDelEqualTo(1).andReportIdEqualTo(taskId);
        statisticsExample.setOrderByClause("statistics_order asc");
        List<ReportStatisticsScore> reportStatisticsScores = reportStatisticsScoreBaseMapper.selectByExample(statisticsExample);
        List<AxisWrapVO> axisWrapVOS = Lists.newArrayList();
        for (ReportStatisticsScore statisticsScore : reportStatisticsScores) {
            AxisWrapVO axisWrapVo = new AxisWrapVO();
            axisWrapVo.setXAxisProduct(statisticsScore.getFieldX());
            axisWrapVo.setYAxisProduct(statisticsScore.getFieldY());
            axisWrapVo.setStatisticsId(statisticsScore.getId());
            axisWrapVo.setOrder(statisticsScore.getStatisticsOrder());
            if (ObjectUtil.notEqual(statisticsScore.getStatus(), 1)) {
                axisWrapVo.setStatisticsDesc(StringUtils.isEmpty(statisticsScore.getStatisticsDesc()) ? "统计异常" : statisticsScore.getStatisticsDesc());
                axisWrapVOS.add(axisWrapVo);
                continue;
            }
            ScoreStatisticsDetailExample detailExample = new ScoreStatisticsDetailExample();
            detailExample.createCriteria().andStatisticsIdEqualTo(statisticsScore.getId()).andIsDelEqualTo(Constants.DATA_VALID);
            List<ScoreStatisticsDetail> details = scoreStatisticsDetailBaseMapper.selectByExample(detailExample);
            switch (statisticsScore.getReportScoreType()) {
                case 1:
                    axisWrapVo.setReportScoreType(1);
                    singleConvert(axisWrapVo, details);
                    break;
                case 2:
                    axisWrapVo.setReportScoreType(2);
                    multipleConvert(axisWrapVo, details);
                    break;
                default:
                    break;
            }
            axisWrapVOS.add(axisWrapVo);
        }
        return axisWrapVOS;
    }

    private void multipleConvert(AxisWrapVO axisWrapVo, List<ScoreStatisticsDetail> details) {
        //1.获取x轴
        List<String> xAxis = getAxais(details, ScoreStatisticsDetail::getFieldXValue);
        //2.获取y轴
        List<String> yStep = getAxais(details, ScoreStatisticsDetail::getFieldYValue);
        //3.按field_y_value把数据分组为多列
        Map<String, Map<String, Integer>> groupedByY = details.stream().collect(Collectors.groupingBy(ScoreStatisticsDetail::getFieldYValue,
            Collectors.toMap(ScoreStatisticsDetail::getFieldXValue, ScoreStatisticsDetail::getFieldNum)));
        //4.构建yAxis列表
        List<WrapDataVO> yAxisData = yStep.stream().map((String yValue) -> {
            List<String> data =
                xAxis.stream().map(xValue -> String.valueOf(groupedByY.getOrDefault(yValue, Collections.emptyMap()).getOrDefault(xValue, 0)))
                    .collect(Collectors.toList());
            //总计数量
            int sum = data.stream().mapToInt(Integer::parseInt).sum();
            data.add(String.valueOf(sum));
            return new WrapDataVO(yValue, data);
        }).collect(Collectors.toList());
        //5.增加行总计
        WrapDataVO yaxisSum = getYaxisSum(details, xAxis);
        yAxisData.add(yaxisSum);
        xAxis.add("总计");
        axisWrapVo.setXAxis(xAxis);
        axisWrapVo.setYAxis(yAxisData);
    }

    private WrapDataVO getYaxisSum(List<ScoreStatisticsDetail> details, List<String> xAxis) {
        // 先计算分组结果
        Map<String, Integer> groupedData = details.stream()
                .collect(Collectors.groupingBy(
                        ScoreStatisticsDetail::getFieldXValue,
                        Collectors.summingInt(ScoreStatisticsDetail::getFieldNum)
                ));
        List<String> data;
        // 检查是否包含中文
        boolean hasChinese = xAxis.stream()
                .anyMatch(com.br.marketing.common.utils.StringUtils::containsChinese);
        if (hasChinese) {
            // 包含中文，直接返回所有值
            data = groupedData.values().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            // 不包含中文，按数字排序
            data = groupedData.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> {
                        String startValue = entry.getKey()
                                .replaceAll("[\\[\\]\\(\\)]", "").split(",")[0];
                        return Double.parseDouble(startValue);
                    }))
                    .map(entry -> entry.getValue().toString())
                    .collect(Collectors.toList());
        }
        data.add(Integer.toString(data.stream().mapToInt(Integer::parseInt).sum()));
        return new WrapDataVO("总计", data);
    }

    /**
     * x轴步长 eg:[0,5),[5,10),[10,15)...
     * y轴模型 eg:scorencashonxchx
     * @param axisWrapVo
     * @param details
     */
    private void singleConvert(AxisWrapVO axisWrapVo, List<ScoreStatisticsDetail> details) {
        //1.获取x轴
        List<String> xAxis = getAxais(details, ScoreStatisticsDetail::getFieldXValue);
        //2.按模型field_y_value把数据分组为多列
        Map<String, Map<String, Integer>> groupedByY = details.stream().collect(Collectors.groupingBy(ScoreStatisticsDetail::getFieldYValue,
            Collectors.toMap(ScoreStatisticsDetail::getFieldXValue, ScoreStatisticsDetail::getFieldNum)));
        //3.构建yAxis列表
        List<String> keys = Splitter.on(",").splitToList(axisWrapVo.getXAxisProduct());
        List<WrapDataVO> yAxis = Lists.newArrayList();
        for (String yName : keys) {
            //每列数据
            Map<String, Integer> columnDetail = groupedByY.getOrDefault(yName, Collections.emptyMap());
            //根据X轴步长 填充Y轴数据
            List<String> data =
                xAxis.stream().map(xValue -> String.valueOf(columnDetail.getOrDefault(xValue, 0)))
                    .collect(Collectors.toList());
            //总计数量
            BigDecimal total = data.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
            data.add(String.valueOf(total));
            WrapDataVO numWrapDataVo = new WrapDataVO(yName, data);
            yAxis.add(numWrapDataVo);
            //计算占比
            List<String> proportion;
            if (total.compareTo(BigDecimal.ZERO) == 0) {
                proportion = data.stream().map(num -> "/").collect(Collectors.toList());
            } else {
                proportion = data.stream().map(BigDecimal::new).map(num -> num.multiply(BigDecimal.valueOf(100)).divide(total, 3, RoundingMode.HALF_UP))
                        .map(percent -> percent.compareTo(BigDecimal.ZERO) == 0 ? "0%" : (percent + "%")).collect(Collectors.toList());
            }
            WrapDataVO proportionWrapDataVo = new WrapDataVO(yName + "占比", proportion);
            yAxis.add(proportionWrapDataVo);
        }
        //设置横纵坐标轴的内容
        xAxis.add("总计");
        axisWrapVo.setXAxis(xAxis);
        axisWrapVo.setYAxis(yAxis);
    }

    /**
     * 返回排序好的坐标
     * @param details
     * @param keyMapper
     * @return
     */
    private List<String> getAxais(List<ScoreStatisticsDetail> details, Function<ScoreStatisticsDetail, String> keyMapper) {
        List<String> distinctAxais = details.stream().map(keyMapper).distinct().collect(Collectors.toList());
        for (String s : distinctAxais) {
            if(com.br.marketing.common.utils.StringUtils.containsChinese(s)){
                return distinctAxais;
            }
        }
        return details.stream()
                .map(keyMapper)
                .distinct()
                .sorted(Comparator.comparing(interval -> {
                    String startValue = interval.replaceAll("[\\[\\]\\(\\)]", "").split(",")[0];
                    return Double.parseDouble(startValue);
                }))
                .collect(Collectors.toList());
    }

    private boolean checkKeys(List<String> keys, List<String> config) {
        List<String> intersection = Lists.newArrayList(keys);
        intersection.retainAll(config);
        return CollectionUtil.isNotEmpty(intersection);
    }

    public void writeDistributedData(ExcelWriter writer, AxisWrapVO data) {
        List<String> xAxis = data.getXAxis();
        List<WrapDataVO> yAxis = data.getYAxis();
        // 只有单模型不输出A1表格内容
        if (data.getReportScoreType() == 2) {
            writer.writeCellValue(0, 0,
                StringUtils.isEmpty(data.getYAxisProduct()) ? data.getXAxisProduct() : (data.getXAxisProduct() + "\\" + data.getYAxisProduct()));
        }
        // 写X轴数据
        for (int i = 0; i < xAxis.size(); i++) {
            writer.writeCellValue(0, i + 1, xAxis.get(i));
        }

        switch (data.getReportScoreType()) {
            case 1:
                writeSingleDataBar(writer, yAxis, xAxis);
                break;
            case 2:
                writeMultipleDataBar(writer, yAxis, xAxis);
                break;
            default:
                break;
        }

        // 自适应宽度
        XSSFSheet sheet = (XSSFSheet)writer.getSheet();
        int columnCount = writer.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            // 调整每一列宽度
            sheet.autoSizeColumn(i);
            // 解决自动设置列宽中文失效的问题
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 15 / 10);
        }
    }

    private void writeSingleDataBar(ExcelWriter writer, List<WrapDataVO> yAxis, List<String> xAxis) {
        Workbook workbook = writer.getWorkbook();
        DataFormat format = workbook.createDataFormat();
        List<String> regions = Lists.newArrayList();
        // 写入Y轴数据
        for (int i = 0; i < yAxis.size(); i++) {
            WrapDataVO yAxi = yAxis.get(i);
            List<String> yData = yAxi.getData();
            // 按列写入
            writer.writeCellValue(i + 1, 0, yAxi.getName());

            // Write the Y-axis data
            for (int j = 0; j < xAxis.size(); j++) {
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "0";
                if (value.contains("%")) {
                    BigDecimal decimal = new BigDecimal(value.replace("%", ""));
                    writer.writeCellValue(i + 1, j + 1, decimal.divide(BigDecimal.valueOf(100), decimal.scale() + 2, RoundingMode.HALF_UP));

                    CellStyle cellStyle = StyleUtil.cloneCellStyle(workbook, writer.getCellStyle());
                    if (decimal.compareTo(BigDecimal.ZERO) == 0) {
                        short formatIndex = format.getFormat("0%");
                        cellStyle.setDataFormat(formatIndex);
                    } else {
                        short formatIndex = format.getFormat("0.000%");
                        cellStyle.setDataFormat(formatIndex);
                    }
                    writer.getCell(i + 1, j + 1).setCellStyle(cellStyle);
                } else {
                    writer.writeCellValue(i + 1, j + 1, value);
                }
            }
            if (i % 2 == 1) {
                String startCell = CellReference.convertNumToColString(i + 1) + (2);
                String endCell = CellReference.convertNumToColString(i + 1) + (xAxis.size() + 1);
                String region = startCell + ":" + endCell;
                regions.add(region);
            }
        }
        DataBarUtil.addMinMaxDataBar(writer, regions);
    }

    private static void writeMultipleDataBar(ExcelWriter writer, List<WrapDataVO> yAxis, List<String> xAxis) {
        for (int i = 0; i < yAxis.size(); i++) {
            WrapDataVO yAxi = yAxis.get(i);
            List<String> yData = yAxi.getData();
            // 按列写入
            writer.writeCellValue(i + 1, 0, yAxi.getName());
            // Write the Y-axis data
            for (int j = 0; j < xAxis.size(); j++) {
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "0";
                writer.writeCellValue(i + 1, j + 1, Long.parseLong(value));
            }
        }
    }

    public void deleteTempFile(String tmpPath) {
        // 删除指定目录
        Path directPath = Paths.get(tmpPath);
        // 删除目录下的所有 xlsx 文件，最后删除目录
        try (Stream<Path> paths = Files.walk(directPath)) {
            paths.sorted((Path path1, Path path2) -> -path1.compareTo(path2)).forEach((Path path) -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.warn("删除临时文件异常,path:{}", tmpPath, e);
                }
            });
        } catch (IOException e) {
            log.warn("删除临时文件异常,path:{}", tmpPath, e);
        }
    }
}
