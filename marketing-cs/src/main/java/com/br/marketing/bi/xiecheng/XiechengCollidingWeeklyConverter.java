package com.br.marketing.bi.xiecheng;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingWeeklyReportDTO;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.SourceStatisticDictMapper;
import com.br.marketing.mapper.XieChengBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportConfigDIctParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import com.google.common.base.Splitter;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 携程7日撞库结果分布报表适配实现
 * @author senyang.zheng
 * @date 2024/09/04
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_COLLIDING_WEEKLY_REPORT)
public class XiechengCollidingWeeklyConverter extends AbstractBiReportConverter<BiReportVO, XiechengCollidingWeeklyReportDTO> {
    @Autowired
    private XieChengBiReportMapper xieChengBiReportMapper;
    @Resource
    private SourceStatisticDictMapper statisticDictMapper;

    /**
     * 获取数据
     * @param param 参数
     * @return {@link List }<{@link XiechengCollidingWeeklyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengCollidingWeeklyReportDTO> fetchData(BiReportParam param) {
        List<XiechengCollidingWeeklyReportDTO> dtos = Lists.newArrayList();
        String lockPeriodStartDate = getDictByKeyAndApiCode("xc_lock_period_start_date", "3710058");
        DateTime startDate = DateUtil.parse(lockPeriodStartDate, "yyyy-MM-dd");
        DateTime currentDate = DateUtil.date(); // 当前时间
        long daysBetween = DateUtil.betweenDay(startDate, currentDate, false); // 计算当前滚动周期的天数
        int currentCycleOffset = (int) (daysBetween / 7); // 计算偏移量
        DateTime currentPeriodStart = DateUtil.offsetDay(startDate, currentCycleOffset * 7); // 当前周期的开始日期

        for (int i = 0; i < 5; i++) {
            DateTime periodStart = DateUtil.offsetDay(currentPeriodStart, -(i + 1) * 7);
            DateTime periodEnd = DateUtil.offsetDay(periodStart, 6);

            // 查询交集量级
            List<SourceStatisticDict> sourceStatisticDicts = getSourceStatisticDicts(periodStart, periodEnd);

            // 根据datapackact分组，对锁定量级求和
            List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS = xieChengBiReportMapper.selectXcCollidingWeeklybI_(periodStart.toDateStr(),
                    periodEnd.toDateStr());

            // 处理"1400wdx"和"1400wlt"数据包
            processSpecialPackets(weeklyReportDTOS, periodStart, periodEnd, sourceStatisticDicts, dtos);

            // 处理其他数据包
            processGeneralPackets(weeklyReportDTOS, periodStart, periodEnd, sourceStatisticDicts, dtos);
        }
        return dtos;
    }

    private List<SourceStatisticDict> getSourceStatisticDicts(DateTime periodStart, DateTime periodEnd) {
        BiReportConfigDIctParam configDIctParam = new BiReportConfigDIctParam();
        configDIctParam.setStartDate(periodStart);
        configDIctParam.setEndDate(periodEnd);
        List<SourceStatisticDict> sourceStatisticDicts = statisticDictMapper.selectListbI_(configDIctParam);
        return sourceStatisticDicts;
    }

    private void processSpecialPackets(List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS, DateTime periodStart, DateTime periodEnd,
                                       List<SourceStatisticDict> sourceStatisticDicts, List<XiechengCollidingWeeklyReportDTO> dtos) {
        List<XiechengCollidingWeeklyReportDTO> wdxAndWltReport = weeklyReportDTOS.stream()
                .filter((XiechengCollidingWeeklyReportDTO t) -> Objects.equals(t.getDataPacket(), "1400wdx") || Objects.equals(t.getDataPacket(),
                        "1400wlt"))
                .collect(Collectors.toList());

        if (!wdxAndWltReport.isEmpty()) {
            XiechengCollidingWeeklyReportDTO dto = createDto("1400wdx&1400wlt", wdxAndWltReport, periodStart, periodEnd);
            assembleAndAddToList(dto, sourceStatisticDicts, dtos);
        }
    }

    private void processGeneralPackets(List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS, DateTime periodStart, DateTime periodEnd,
                                       List<SourceStatisticDict> sourceStatisticDicts, List<XiechengCollidingWeeklyReportDTO> dtos) {
        weeklyReportDTOS.stream()
                .filter(t -> !Objects.equals(t.getDataPacket(), "1400wdx") && !Objects.equals(t.getDataPacket(), "1400wlt"))
                .forEach(weeklyReportDTO -> {
                    XiechengCollidingWeeklyReportDTO genDto = createDto(weeklyReportDTO.getDataPacket(), weeklyReportDTO.getLockNum(), periodStart,
                            periodEnd);
                    assembleAndAddToList(genDto, sourceStatisticDicts, dtos);
                });
    }

    private XiechengCollidingWeeklyReportDTO createDto(String dataPacket, List<XiechengCollidingWeeklyReportDTO> reportList,
                                                       DateTime periodStart, DateTime periodEnd) {
        XiechengCollidingWeeklyReportDTO dto = new XiechengCollidingWeeklyReportDTO();
        dto.setDataPacket(dataPacket);
        dto.setLockPeriod(formatPeriod(periodStart, periodEnd));
        dto.setLockNum(reportList.stream().mapToLong(XiechengCollidingWeeklyReportDTO::getLockNum).sum());
        return dto;
    }

    private XiechengCollidingWeeklyReportDTO createDto(String dataPacket, long lockNum, DateTime periodStart, DateTime periodEnd) {
        XiechengCollidingWeeklyReportDTO dto = new XiechengCollidingWeeklyReportDTO();
        dto.setDataPacket(dataPacket);
        dto.setLockPeriod(formatPeriod(periodStart, periodEnd));
        dto.setLockNum(lockNum);
        return dto;
    }

    private String formatPeriod(DateTime start, DateTime end) {
        return DateUtil.format(start, "yyyy-MM-dd") + " ~ " + DateUtil.format(end, "yyyy-MM-dd");
    }

    private void assembleAndAddToList(XiechengCollidingWeeklyReportDTO dto, List<SourceStatisticDict> sourceStatisticDicts,
                                      List<XiechengCollidingWeeklyReportDTO> dtos) {
        String dictKey = "xc_dataPacket_intersection_" + dto.getDataPacket();
        Long intersectionNum = sourceStatisticDicts.stream()
                .filter((SourceStatisticDict t) -> Objects.equals(dictKey, t.getDictKey()))
                .map(SourceStatisticDict::getDictValue)
                .map(Long::parseLong)
                .findFirst()
                .orElse(0L);
        dto.setIntersectionNum(intersectionNum);

        if (intersectionNum != 0) {
            dto.setCollidingBackRatio(new BigDecimal(dto.getLockNum()).divide(new BigDecimal(intersectionNum), 2, RoundingMode.FLOOR)
                    .multiply(new BigDecimal(100)));
        } else {
            dto.setCollidingBackRatio(BigDecimal.ZERO);
        }
        dtos.add(dto);
    }

    /**
     * 数据处理
     * @param dtos   数据
     * @param extend 扩展参数
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public BiReportVO process(List<XiechengCollidingWeeklyReportDTO> dtos, JSONObject extend) {
        DateTime lockPeriodStartDate = DateUtil.parse(getDictByKeyAndApiCode("xc_lock_period_start_date", "3710058"), "yyyy-MM-dd");
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_COLLIDING_WEEKLY_REPORT.getTypeName());
        biReportVO.setReportName("7日撞库结果分布");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据标签排序，添加空值处理
        dtos.sort(Comparator.comparing(XiechengCollidingWeeklyReportDTO::getDataPacket, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(XiechengCollidingWeeklyReportDTO::getIntersectionNum, Comparator.nullsLast(Comparator.naturalOrder())));
        // 按照标签维度做横坐标
        List<String> xAxis =
                dtos.stream().map(report -> report.getDataPacket() + "_" + report.getIntersectionNum()).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("dataPacket_intersectionNum");
        biReportVO.setXAxis(xAxis);

        // 初始化Y轴数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        // 1. 根据 lockPeriod 分组获取 Map<String, List<XiechengCollidingWeeklyReportDTO>> lockPeriodDataMap
        Map<String, List<XiechengCollidingWeeklyReportDTO>> lockPeriodDataMap =
                dtos.stream().collect(Collectors.groupingBy(XiechengCollidingWeeklyReportDTO::getLockPeriod));
        // 1.1 对lockPeriodDataMap的key和value进行排序
        Map<String, List<XiechengCollidingWeeklyReportDTO>> sortedLockPeriodDataMap = getSortedLockPeriodDataMap(lockPeriodDataMap);
        // 2. 遍历 lockPeriodDataMap
        for (Map.Entry<String, List<XiechengCollidingWeeklyReportDTO>> entry : sortedLockPeriodDataMap.entrySet()) {
            String lockPeriod = entry.getKey();
            DateTime currentLockPeriodStart = DateUtil.parse(Splitter.on("~").splitToList(lockPeriod).get(0), "yyyy-MM-dd");
            // 计算当前滚动周期的开始和设定日期间隔天数
            long daysBetween = DateUtil.betweenDay(lockPeriodStartDate, currentLockPeriodStart, false);
            // 计算当前滚动周期的偏移量
            int offset = (int) (daysBetween / 7);
            List<XiechengCollidingWeeklyReportDTO> group = entry.getValue();
            yAxis.add(buildWrapDataVO("第" + offset + "次锁定周期", group, XiechengCollidingWeeklyReportDTO::getLockPeriod, FormatType.DEFAULT));
            yAxis.add(buildWrapDataVO("锁定量级", group, XiechengCollidingWeeklyReportDTO::getLockNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("撞回率", group, XiechengCollidingWeeklyReportDTO::getCollidingBackRatio, FormatType.PERCENT_SIGN));
        }
        biReportVO.setYAxis(yAxis);
        return biReportVO;
    }

    private Map<String, List<XiechengCollidingWeeklyReportDTO>>
    getSortedLockPeriodDataMap(Map<String, List<XiechengCollidingWeeklyReportDTO>> lockPeriodDataMap) {
        Map<String, List<XiechengCollidingWeeklyReportDTO>> sortedLockPeriodDataMap = new TreeMap<>(lockPeriodDataMap);
        sortedLockPeriodDataMap.forEach((key, valueList) -> valueList
                .sort(Comparator.comparing(XiechengCollidingWeeklyReportDTO::getDataPacket, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(XiechengCollidingWeeklyReportDTO::getIntersectionNum, Comparator.nullsLast(Comparator.naturalOrder()))));
        return sortedLockPeriodDataMap;
    }

    /**
     * 导出数据
     * @param excelWriter excelWriter
     * @param param       参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    @Override
    public void exportData(ExcelWriter excelWriter, BiReportDownLoadParam param) {
        // excel sheet名称最大长度31，超出31截取前31位
        String sheetName = param.getReportName().length() > 31 ? param.getReportName().substring(0, 31) : param.getReportName();
        excelWriter.setSheet(sheetName);
        // 数据写入
        writeData(excelWriter, param);
        // 剔除默认生成的第一个sheet
        excelWriter.getWorkbook().removeSheetAt(0);
    }

    /**
     * 写入数据
     * @param writer writer
     * @param param  参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    private void writeData(ExcelWriter writer, BiReportDownLoadParam param) {
        List<String> xAxis = param.getXAxis();
        List<WrapDataVO> yAxis = param.getYAxis();
        // 写入X轴名称
        List<String> tagNames = Splitter.on("_").splitToList(param.getXAxisName());
        for (int i = 0; i < tagNames.size(); i++) {
            writer.writeCellValue(i, 0, tagNames.get(i));
        }
        // 写X轴数据
        long totalIntersectionNum = 0L;
        for (int i = 0; i < xAxis.size(); i++) {
            List<String> tags = Splitter.on("_").splitToList(xAxis.get(i));
            for (int j = 0; j < tags.size(); j++) {
                writer.writeCellValue(j, i + 1, Objects.equals("null", tags.get(j)) ? "空" : tags.get(j));
                if (j == 1) {
                    totalIntersectionNum += Long.parseLong(tags.get(j).replaceAll(",", ""));
                }
            }
        }
        writer.writeCellValue(0, xAxis.size() + 1, "总计");
        writer.writeCellValue(1, xAxis.size() + 1, String.format(Locale.getDefault(), "%,d", totalIntersectionNum));
        // 写入Y轴数据
        for (int i = 0; i < yAxis.size(); i++) {
            WrapDataVO yAxi = yAxis.get(i);
            List<String> yData = yAxi.getData();
            long totalLockNum = 0L;
            // 写入Y轴名称
            writer.writeCellValue(i + 2, 0, yAxi.getName());
            // 写入Y轴数据
            for (int j = 0; j < xAxis.size(); j++) {
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "0";
                if (i % 3 == 1) {
                    // 求和时处理千分位
                    totalLockNum += Long.parseLong(yData.get(j).replaceAll(",", ""));
                }
                writer.writeCellValue(i + 2, j + 1, value);
            }
            if (i % 3 == 1) {
                writer.writeCellValue(i + 2, yData.size() + 1, String.format(Locale.getDefault(), "%,d", totalLockNum));
            } else {
                writer.writeCellValue(i + 2, yData.size() + 1, "");
            }
        }
        // 自适应宽度
        writer.autoSizeColumnAll();
    }
}
