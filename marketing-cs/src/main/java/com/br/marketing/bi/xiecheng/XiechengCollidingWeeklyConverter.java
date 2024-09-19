package com.br.marketing.bi.xiecheng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
import com.br.marketing.vo.bi.param.BiReportConfigDictParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import com.google.common.base.Splitter;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelWriter;
import groovy.util.logging.Slf4j;

/**
 * 携程7日撞库结果分布报表适配实现
 * 
 * @author senyang.zheng
 * @date 2024/09/04
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_COLLIDING_WEEKLY_REPORT)
public class XiechengCollidingWeeklyConverter extends AbstractBiReportConverter<BiReportVO, XiechengCollidingWeeklyReportDTO> {

    @Resource
    private XieChengBiReportMapper xieChengBiReportMapper;

    @Resource
    private SourceStatisticDictMapper statisticDictMapper;

    /**
     * 获取数据
     * 
     * @param param 参数
     * @return {@link List }<{@link XiechengCollidingWeeklyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengCollidingWeeklyReportDTO> fetchData(BiReportParam param) {
        List<XiechengCollidingWeeklyReportDTO> dtos = Lists.newArrayList();
        String lockPeriodStartDate = getDictByKeyAndApiCode("xc_lock_period_start_date", null);
        DateTime startDate = DateUtil.parse(lockPeriodStartDate, "yyyy-MM-dd");
        DateTime currentDate = DateUtil.date();
        long daysBetween = DateUtil.betweenDay(startDate, currentDate, false);
        int currentCycleOffset = (int)(daysBetween / 7);
        DateTime currentPeriodStart = DateUtil.offsetDay(startDate, currentCycleOffset * 7);

        for (int i = 0; i < 5; i++) {
            DateTime periodStart = DateUtil.offsetDay(currentPeriodStart, -i * 7);
            DateTime periodEnd = DateUtil.offsetDay(periodStart, 6);

            // 查询交集量级
            List<SourceStatisticDict> sourceStatisticDicts = getSourceStatisticDicts(periodStart, periodEnd);

            // 根据datapackact分组
            List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS =
                xieChengBiReportMapper.selectXcCollidingWeeklybI_(periodStart.toDateStr(), periodEnd.toDateStr());

            // 处理"1400wdx"和"1400wlt"数据包
            processSpecialPackets(weeklyReportDTOS, periodStart, periodEnd, sourceStatisticDicts, dtos);

            // 处理其他数据包
            processGeneralPackets(weeklyReportDTOS, periodStart, periodEnd, sourceStatisticDicts, dtos);
        }
        return dtos;
    }

    private List<SourceStatisticDict> getSourceStatisticDicts(DateTime periodStart, DateTime periodEnd) {
        BiReportConfigDictParam configDIctParam = new BiReportConfigDictParam();
        configDIctParam.setStartDate(periodStart);
        configDIctParam.setEndDate(periodEnd);
        return statisticDictMapper.selectListbI_(configDIctParam);
    }

    private void processSpecialPackets(List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS, DateTime periodStart, DateTime periodEnd,
        List<SourceStatisticDict> sourceStatisticDicts, List<XiechengCollidingWeeklyReportDTO> dtos) {
        List<XiechengCollidingWeeklyReportDTO> wdxAndWltReport = weeklyReportDTOS.stream()
            .filter(
                (XiechengCollidingWeeklyReportDTO t) -> Objects.equals(t.getDataPacket(), "1400wdx") || Objects.equals(t.getDataPacket(), "1400wlt"))
            .collect(Collectors.toList());

        if (!wdxAndWltReport.isEmpty()) {
            XiechengCollidingWeeklyReportDTO dto = createDto("1400wdx&1400wlt", wdxAndWltReport, periodStart, periodEnd);
            assembleAndAddToList(dto, sourceStatisticDicts, dtos);
        }
    }

    private void processGeneralPackets(List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS, DateTime periodStart, DateTime periodEnd,
        List<SourceStatisticDict> sourceStatisticDicts, List<XiechengCollidingWeeklyReportDTO> dtos) {
        weeklyReportDTOS.stream().filter(t -> !Objects.equals(t.getDataPacket(), "1400wdx") && !Objects.equals(t.getDataPacket(), "1400wlt"))
            .forEach((XiechengCollidingWeeklyReportDTO weeklyReportDTO) -> {
                XiechengCollidingWeeklyReportDTO genDto =
                    createDto(weeklyReportDTO.getDataPacket(), weeklyReportDTO.getLockNum(), periodStart, periodEnd);
                assembleAndAddToList(genDto, sourceStatisticDicts, dtos);
            });
    }

    private XiechengCollidingWeeklyReportDTO createDto(String dataPacket, List<XiechengCollidingWeeklyReportDTO> reportList, DateTime periodStart,
        DateTime periodEnd) {
        XiechengCollidingWeeklyReportDTO dto = new XiechengCollidingWeeklyReportDTO();
        dto.setDataPacket(dataPacket);
        dto.setLockPeriod(formatPeriod(periodStart, periodEnd));
        // 对锁定量级求和
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
        Long intersectionNum = sourceStatisticDicts.stream().filter((SourceStatisticDict t) -> Objects.equals(dictKey, t.getDictKey()))
            .map(SourceStatisticDict::getDictValue).map(Long::parseLong).findFirst().orElse(0L);
        dto.setIntersectionNum(intersectionNum);
        if (intersectionNum != 0) {
            dto.setCollidingBackRatio(new BigDecimal(dto.getLockNum()).divide(new BigDecimal(intersectionNum), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP));
        } else {
            dto.setCollidingBackRatio(BigDecimal.ZERO);
        }
        dtos.add(dto);
    }

    /**
     * 数据处理
     * 
     * @param dtos 数据
     * @param extend 扩展参数
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<BiReportVO> process(List<XiechengCollidingWeeklyReportDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        DateTime lockPeriodStartDate = DateUtil.parse(getDictByKeyAndApiCode("xc_lock_period_start_date", "3710058"), "yyyy-MM-dd");
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_COLLIDING_WEEKLY_REPORT.getTypeName());
        biReportVO.setReportName("7日撞库结果分布");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据标签排序，添加空值处理
        dtos.sort(Comparator.comparing(XiechengCollidingWeeklyReportDTO::getDataPacket, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(XiechengCollidingWeeklyReportDTO::getIntersectionNum, Comparator.nullsLast(Comparator.naturalOrder())));
        // 按照标签维度做横坐标
        List<String> xAxis = dtos.stream()
            .map(report -> report.getDataPacket() + SEPARATOR
                + (report.getIntersectionNum() == null ? "0" : String.format(Locale.getDefault(), "%,d", report.getIntersectionNum())))
            .distinct().collect(Collectors.toList());
        // 计算交集量级总计 现根据dataPacket获取去重后的量级再求和
        Map<String, Long> totalIntersectionByDataPacket = dtos.stream().collect(Collectors.toMap(XiechengCollidingWeeklyReportDTO::getDataPacket,
            XiechengCollidingWeeklyReportDTO::getIntersectionNum, (existing, replacement) -> existing));
        long totalIntersectionNum = totalIntersectionByDataPacket.values().stream().mapToLong(Long::longValue).sum();
        xAxis.add("总计" + SEPARATOR + String.format(Locale.getDefault(), "%,d", totalIntersectionNum));
        biReportVO.setXAxisName("dataPacket" + SEPARATOR + "交集量级");
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
            int offset = (int)(daysBetween / 7) + 1;
            List<XiechengCollidingWeeklyReportDTO> group = entry.getValue();
            WrapDataVO lockPeriodWrapDataVO =
                buildWrapDataVO("第" + offset + "次锁定周期", group, XiechengCollidingWeeklyReportDTO::getLockPeriod, FormatType.DEFAULT);
            lockPeriodWrapDataVO.getData().add("");
            yAxis.add(lockPeriodWrapDataVO);
            WrapDataVO lockNumWrapDataVO =
                buildWrapDataVO("锁定量级", group, XiechengCollidingWeeklyReportDTO::getLockNum, FormatType.THOUSAND_SEPARATOR);
            // 锁定量级总计
            long lockNumSum =
                group.stream().mapToLong((XiechengCollidingWeeklyReportDTO dto) -> dto.getLockNum() == null ? 0 : dto.getLockNum()).sum();
            lockNumWrapDataVO.getData().add(String.format(Locale.getDefault(), "%,d", lockNumSum));
            yAxis.add(lockNumWrapDataVO);

            // 总计撞回率
            WrapDataVO collidingBackRatioWrapDataVO =
                buildWrapDataVO("撞回率", group, XiechengCollidingWeeklyReportDTO::getCollidingBackRatio, FormatType.PERCENT_SIGN);
            BigDecimal collidingBackRatioTotal = new BigDecimal(lockNumSum).divide(new BigDecimal(totalIntersectionNum), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
            collidingBackRatioWrapDataVO.getData().add(collidingBackRatioTotal + "%");
            yAxis.add(collidingBackRatioWrapDataVO);
        }
        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
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
     * 
     * @param excelWriter excelWriter
     * @param params 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    @Override
    public void exportData(ExcelWriter excelWriter, List<BiReportDownLoadParam> params) {
        // excel sheet名称最大长度31，超出31截取前31位
        String sheetName =
            params.get(0).getReportName().length() > 31 ? params.get(0).getReportName().substring(0, 31) : params.get(0).getReportName();
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
        for (BiReportDownLoadParam param : params) {
            List<String> xAxis = param.getXAxis();
            List<WrapDataVO> yAxis = param.getYAxis();
            // 写入X轴名称
            List<String> tagNames = Splitter.on(SEPARATOR).splitToList(param.getXAxisName());
            for (int i = 0; i < tagNames.size(); i++) {
                writer.writeCellValue(i, rowIndex, tagNames.get(i));
            }
            // 写X轴数据
            for (int i = 0; i < xAxis.size(); i++) {
                List<String> tags = Splitter.on(SEPARATOR).splitToList(xAxis.get(i));
                for (int j = 0; j < tags.size(); j++) {
                    writer.writeCellValue(j, rowIndex + i + 1, Objects.equals("null", tags.get(j)) ? "空" : tags.get(j));
                }
            }
            // 写入Y轴数据
            for (int i = 0; i < yAxis.size(); i++) {
                WrapDataVO yAxi = yAxis.get(i);
                List<String> yData = yAxi.getData();
                // 写入Y轴名称
                writer.writeCellValue(i + 2, rowIndex, yAxi.getName());
                // 写入Y轴数据
                for (int j = 0; j < xAxis.size(); j++) {
                    String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "";
                    writer.writeCellValue(i + 2, rowIndex + j + 1, value);
                }
            }
            // 添加空行 xAxis.size() + 1 为当前表格所占行数，再+1添加空行
            rowIndex += xAxis.size() + 2;
        }
        // 自适应宽度
        autoSizeColumnAll(writer);
    }
}
