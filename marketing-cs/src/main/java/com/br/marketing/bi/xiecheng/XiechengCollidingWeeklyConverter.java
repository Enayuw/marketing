package com.br.marketing.bi.xiecheng;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import com.google.common.base.Splitter;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
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
        // 当前时间
        DateTime currentDate = DateUtil.date();
        // 计算当前滚动周期的开始和结束日期
        long daysBetween = DateUtil.betweenDay(startDate, currentDate, false);
        // 计算当前滚动周期的偏移量
        int currentCycleOffset = (int) (daysBetween / 7);
        // 获取当前滚动周期的开始日期和结束日期
        DateTime currentPeriodStart = DateUtil.offsetDay(startDate, currentCycleOffset * 7);
        // 查询静态变量
        List<SourceStatisticDict> sourceStatisticDicts = statisticDictMapper.selectListbI_(null, null);
        for (int i = 0; i < 5; i++) {
            DateTime periodStart = DateUtil.offsetDay(currentPeriodStart, -(i + 1) * 7);
            DateTime periodEnd = DateUtil.offsetDay(periodStart, 6);

            List<XiechengCollidingWeeklyReportDTO> weeklyReportDTOS = xieChengBiReportMapper.selectXcCollidingWeeklybI_(periodStart.toDateStr(),
                    periodEnd.toDateStr());

            List<XiechengCollidingWeeklyReportDTO> wdxAndWltReport = weeklyReportDTOS.stream().filter(t -> Objects.equals(t.getDataPacket(),
                    "1400wdx")
                    || Objects.equals(t.getDataPacket(),
                    "1400wlt")).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(wdxAndWltReport)) {
                XiechengCollidingWeeklyReportDTO dto = new XiechengCollidingWeeklyReportDTO();
                dto.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
                dto.setDataPacket("1400wdx&1400wlt");
                long wdxAndwltLockNum = wdxAndWltReport.stream().mapToLong(XiechengCollidingWeeklyReportDTO::getLockNum).sum();
                dto.setLockNum(wdxAndwltLockNum);

                assebleAndAddToList(dto, sourceStatisticDicts, dtos);
            }

            List<XiechengCollidingWeeklyReportDTO> generalReport = weeklyReportDTOS.stream().filter(t -> !Objects.equals(t.getDataPacket(),
                    "1400wdx")
                    && !Objects.equals(t.getDataPacket(),
                    "1400wlt")).collect(Collectors.toList());

            for (XiechengCollidingWeeklyReportDTO weeklyReportDTO : generalReport) {
                XiechengCollidingWeeklyReportDTO genDto = new XiechengCollidingWeeklyReportDTO();
                genDto.setLockPeriod(DateUtil.format(periodStart, "yyyy-MM-dd") + " ~ " + DateUtil.format(periodEnd, "yyyy-MM-dd"));
                genDto.setDataPacket(weeklyReportDTO.getDataPacket());
                genDto.setLockNum(weeklyReportDTO.getLockNum());
                assebleAndAddToList(genDto, sourceStatisticDicts, dtos);
            }
        }
        return dtos;
    }

    private void assebleAndAddToList(XiechengCollidingWeeklyReportDTO genDto, List<SourceStatisticDict> sourceStatisticDicts,
                                     List<XiechengCollidingWeeklyReportDTO> dtos) {
        String dictKey = "xc_dataPacket_intersection_" + genDto.getDataPacket();
        List<String> dictValue =
                sourceStatisticDicts.stream().filter(t -> Objects.equals(dictKey, t.getDictKey())).map(SourceStatisticDict::getDictValue).collect(Collectors.toList());
        Long intersectionNum = CollectionUtils.isEmpty(dictValue) ? 0L : Long.valueOf(dictValue.get(0));
        genDto.setIntersectionNum(intersectionNum);

        genDto.setCollidingBackRatio(new BigDecimal(genDto.getLockNum()).divide(new BigDecimal(genDto.getIntersectionNum()), 2,
                        RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
        dtos.add(genDto);
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
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_COLLIDING_DAILY_REPORT.getTypeName());
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
}
