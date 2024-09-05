package com.br.marketing.bi.xiecheng;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingWeeklyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        Random random = new Random();
        String lockPeriodStartDate = getDictByKeyAndApiCode("xc_lock_period_start_date", "3710058");
        DateTime startDate = DateUtil.parse(lockPeriodStartDate, "yyyy-MM-dd");
        // 当前时间
        DateTime currentDate = DateUtil.date();
        // 计算当前滚动周期的开始和结束日期
        long daysBetween = DateUtil.betweenDay(startDate, currentDate, false);
        // 计算当前滚动周期的偏移量
        int currentCycleOffset = (int) (daysBetween / 7);
        // 获取当前滚动周期的开始日期和结束日期
        DateTime currentPeriodStart = DateUtil.offsetDay(startDate, currentCycleOffset * 7);

        for (int i = 0; i < 5; i++) {
            DateTime periodStart = DateUtil.offsetDay(currentPeriodStart, -(i + 1) * 7);
            DateTime periodEnd = DateUtil.offsetDay(periodStart, 6);

            XiechengCollidingWeeklyReportDTO dto = new XiechengCollidingWeeklyReportDTO();
            dto.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto.setDataPacket("1400wdx&1400wlt");
            dto.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto.setIntersectionNum(10000L);
            dto.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto);

            XiechengCollidingWeeklyReportDTO dto2 = new XiechengCollidingWeeklyReportDTO();
            dto2.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto2.setDataPacket("1200w");
            dto2.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto2.setIntersectionNum(20000L);
            dto2.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto2);

            XiechengCollidingWeeklyReportDTO dto3 = new XiechengCollidingWeeklyReportDTO();
            dto3.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto3.setDataPacket("2800w");
            dto3.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto3.setIntersectionNum(30000L);
            dto3.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto3);

            XiechengCollidingWeeklyReportDTO dto4 = new XiechengCollidingWeeklyReportDTO();
            dto4.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto4.setDataPacket("300w");
            dto4.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto4.setIntersectionNum(40000L);
            dto4.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto4);

            XiechengCollidingWeeklyReportDTO dto5 = new XiechengCollidingWeeklyReportDTO();
            dto5.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto5.setDataPacket("800w");
            dto5.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto5.setIntersectionNum(50000L);
            dto5.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto5);

            XiechengCollidingWeeklyReportDTO dto6 = new XiechengCollidingWeeklyReportDTO();
            dto6.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto6.setDataPacket("900w");
            dto6.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto6.setIntersectionNum(60000L);
            dto6.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto6);

            XiechengCollidingWeeklyReportDTO dto7 = new XiechengCollidingWeeklyReportDTO();
            dto7.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto7.setDataPacket("3300w");
            dto7.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto7.setIntersectionNum(70000L);
            dto7.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto7);

            XiechengCollidingWeeklyReportDTO dto8 = new XiechengCollidingWeeklyReportDTO();
            dto8.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto8.setDataPacket("3500w");
            dto8.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto8.setIntersectionNum(80000L);
            dto8.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto8);

            XiechengCollidingWeeklyReportDTO dto9 = new XiechengCollidingWeeklyReportDTO();
            dto9.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto9.setDataPacket("360w");
            dto9.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto9.setIntersectionNum(90000L);
            dto9.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto9);

            XiechengCollidingWeeklyReportDTO dto10 = new XiechengCollidingWeeklyReportDTO();
            dto10.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
            dto10.setDataPacket("830w");
            dto10.setCollidingBackRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dto10.setIntersectionNum(100000L);
            dto10.setLockNum((long) random.nextInt(5000000));
            dtos.add(dto10);
        }
        return dtos;

    }

    /**
     * 数据处理
     *
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
        dtos.sort(Comparator.comparing(XiechengCollidingWeeklyReportDTO::getDataPacket, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(XiechengCollidingWeeklyReportDTO::getIntersectionNum, Comparator.nullsLast(Comparator.naturalOrder())));
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

    private Map<String, List<XiechengCollidingWeeklyReportDTO>> getSortedLockPeriodDataMap(Map<String, List<XiechengCollidingWeeklyReportDTO>> lockPeriodDataMap) {
        Map<String, List<XiechengCollidingWeeklyReportDTO>> sortedLockPeriodDataMap = new TreeMap<>(lockPeriodDataMap);
        sortedLockPeriodDataMap.forEach((key, valueList) -> valueList.sort(Comparator.comparing(XiechengCollidingWeeklyReportDTO::getDataPacket,
                Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(XiechengCollidingWeeklyReportDTO::getIntersectionNum,
                Comparator.nullsLast(Comparator.naturalOrder()))));
        return sortedLockPeriodDataMap;
    }

    /**
     * 导出数据
     *
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
     *
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
