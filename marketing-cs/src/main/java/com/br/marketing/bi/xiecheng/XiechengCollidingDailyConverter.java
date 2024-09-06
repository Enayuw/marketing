package com.br.marketing.bi.xiecheng;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengCollidingDailyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.proxy.XiechengBiReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.common.base.Splitter;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程单日撞库结果分布报表适配实现
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_COLLIDING_DAILY_REPORT)
public class XiechengCollidingDailyConverter extends AbstractBiReportConverter<BiReportVO, XiechengCollidingDailyReportDTO> {

    @Resource
    private XiechengBiReportService xiechengBiReportService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取数据
     *
     * @param param 参数
     * @return {@link List }<{@link XiechengCollidingDailyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengCollidingDailyReportDTO> fetchData(BiReportParam param) {
        Map<String, Integer> xiechengBiReportShowNumMap = marketingCommonConfig.getXiechengBiReportShowNumMap();
        Integer distrubuteDayCount = xiechengBiReportShowNumMap.getOrDefault("distrubuteDayCount", 8);
        String reportDateStart = LocalDate.now().minusDays(distrubuteDayCount).toString();
        String reportDateEnd = LocalDate.now().minusDays(1).toString();
        return xiechengBiReportService.selectXcColldingDistrubuteDayList(reportDateStart, reportDateEnd);
    }

    /**
     * 数据处理
     *
     * @param dtos 数据
     * @param extend 自定义参数
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public BiReportVO process(List<XiechengCollidingDailyReportDTO> dtos, JSONObject extend) {
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_COLLIDING_DAILY_REPORT.getTypeName());
        biReportVO.setReportName("单日撞库结果分布");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据标签排序，添加空值处理
        Map<String, Integer> dataPacketorderMap = marketingCommonConfig.getXiechengBiReportDistrubuteDayDataPacketOrderMap();
        dtos.sort(Comparator.comparingInt((XiechengCollidingDailyReportDTO dto) -> dataPacketorderMap.getOrDefault(dto.getDataPacket(), 99))
            .thenComparing(XiechengCollidingDailyReportDTO::getOrgChannel, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(XiechengCollidingDailyReportDTO::getInfo, Comparator.nullsLast(Comparator.naturalOrder())));
        // 按照标签维度做横坐标
        List<String> xAxis = dtos.stream().map(report -> report.getDataPacket() + "_" + report.getOrgChannel() + "_" + report.getInfo()).distinct()
            .collect(Collectors.toList());
        biReportVO.setXAxisName("dataPacket_orgChannel_info");
        biReportVO.setXAxis(xAxis);
        /*
         * 组装数据
         * 数据格式 Map<日期, Map<dataPacket_orgChannel_info, 量级>> reportDateToDataMap
         *
         */
        Map<String,
            Map<String, Long>> reportDateDataMap = dtos.stream()
                .collect(Collectors.groupingBy(XiechengCollidingDailyReportDTO::getReportDate,
                    Collectors.toMap(dto -> dto.getDataPacket() + "_" + dto.getOrgChannel() + "_" + dto.getInfo(),
                        XiechengCollidingDailyReportDTO::getLockNum, (oldValue, newValue) -> newValue, LinkedHashMap::new)));
        /*
         * 构造Y轴数据
         * 1.Map<日期, Map<dataPacket_orgChannel_info, 量级>> reportDateToDataMap先根据日期排序
         * 2.遍历reportDateToDataMap key为WrapDataVO中name字段
         * 3.根据X轴顺序构造WrapDataVO中List<String> data
         */
        List<WrapDataVO> yAxis =
            reportDateDataMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map((Map.Entry<String, Map<String, Long>> entry) -> {
                String reportDate = entry.getKey();
                Map<String, Long> dataMap = entry.getValue();
                // 依据X轴顺序构造List<String> data,若根据X轴未匹配到数据写入默认值0
                List<String> data = xAxis.stream().map(axis -> String.format(Locale.getDefault(), "%,d", dataMap.getOrDefault(axis, 0L)))
                    .collect(Collectors.toList());
                return new WrapDataVO(reportDate, data);
            }).collect(Collectors.toList());
        biReportVO.setYAxis(yAxis);
        return biReportVO;
    }

    private int getOrderScore(String dataPacket) {
        int orderScore;
        switch (dataPacket) {
            case "1400wdx":
                orderScore = 1;
                break;
            case "1400wlt":
                orderScore = 2;
                break;
            case "1200w":
                orderScore = 3;
                break;
            case "2800w":
                orderScore = 4;
                break;
            case "300w":
                orderScore = 5;
                break;
            case "800w":
                orderScore = 6;
                break;
            case "900w":
                orderScore = 7;
                break;
            case "3300w":
                orderScore = 8;
                break;
            case "3500w":
                orderScore = 9;
                break;
            case "360w":
                orderScore = 10;
                break;
            case "830w":
                orderScore = 11;
                break;
            default:
                orderScore = 12;
        }
        return orderScore;
    }

    /**
     * 导出数据
     *
     * @param excelWriter excelWriter
     * @param param 参数
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
     * @param param 参数
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
        for (int i = 0; i < xAxis.size(); i++) {
            List<String> tags = Splitter.on("_").splitToList(xAxis.get(i));
            for (int j = 0; j < tags.size(); j++) {
                writer.writeCellValue(j, i + 1, Objects.equals("null", tags.get(j)) ? "空" : tags.get(j));
            }
        }
        writer.merge(xAxis.size() + 1, xAxis.size() + 1, 0, 2, "总计", false);
        // 写入Y轴数据
        for (int i = 0; i < yAxis.size(); i++) {
            WrapDataVO yAxi = yAxis.get(i);
            List<String> yData = yAxi.getData();
            long total = 0L;
            // 写入Y轴名称
            writer.writeCellValue(i + 3, 0, yAxi.getName());
            // 写入Y轴数据
            for (int j = 0; j < xAxis.size(); j++) {
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "0";
                // 求和时处理千分位
                total += Long.parseLong(yData.get(j).replaceAll(",", ""));
                writer.writeCellValue(i + 3, j + 1, value);
            }
            writer.writeCellValue(i + 3, yData.size() + 1, String.format(Locale.getDefault(), "%,d", total));
        }
        // 自适应宽度
        autoSizeColumnAll(writer);
    }
}
