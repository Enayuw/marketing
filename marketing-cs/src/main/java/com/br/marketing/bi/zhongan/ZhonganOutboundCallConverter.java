package com.br.marketing.bi.zhongan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import cn.hutool.poi.excel.ExcelWriter;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhonganOutboundCallReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 外呼统计报表报表适配实现
 *
 * @author kongbx
 * @date 2024/09/21
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.OUTBOUND_STAT_REPORT)
public class ZhonganOutboundCallConverter extends AbstractBiReportConverter<BiReportVO, ZhonganOutboundCallReportDTO> {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;

    /**
     * 获取数据
     *
     * @param param 参数
     * @return {@link List }<{@link ZhonganOutboundCallReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<ZhonganOutboundCallReportDTO> fetchData(BiReportParam param) {
        JSONObject condition = param.getCondition();
        String userType = condition.getString("userType");
        String month = condition.getString("month");
        if (StringUtils.isEmpty(userType)) {
            return new ArrayList<>();
        }
        List<Integer> userTypes = new ArrayList<>();
        if (userType.contains(",")) {
            userTypes = Arrays.stream(userType.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        } else {
            userTypes.add(Integer.valueOf(userType));
        }
        return zhongAnBiReportMapper.selectZaOutboundCallListbI_(month, userTypes);
    }

    @Override
    public List<BiReportVO> process(List<ZhonganOutboundCallReportDTO> dtos, JSONObject extend) {

        Map<String, List<ZhonganOutboundCallReportDTO>> scoreMap =
            dtos.stream().collect(Collectors.groupingBy(ZhonganOutboundCallReportDTO::getUserType));

        List<BiReportVO> biReportVOList = Lists.newArrayList();
        checkEmpty(scoreMap);
        for (Map.Entry<String, List<ZhonganOutboundCallReportDTO>> entry : scoreMap.entrySet()) {
            BiReportVO biReportVO = new BiReportVO();
            biReportVO.setReportTypeName(BiReportTypeEnum.OUTBOUND_STAT_REPORT.getTypeName());
            biReportVO.setReportName("外呼统计报表");
            biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
            biReportVO.setGroup(entry.getKey());
            // 根据时间和维度排序
            List<ZhonganOutboundCallReportDTO> sortedData = entry.getValue().stream()
                    .sorted(Comparator.comparing(ZhonganOutboundCallReportDTO::getReportDate, Comparator.naturalOrder())
                            .thenComparing(ZhonganOutboundCallReportDTO::getDimension, Comparator.naturalOrder()))
                    .collect(Collectors.toList());

            // 构造横坐标数据
            List<String> xAxis = sortedData.stream().map(ZhonganOutboundCallReportDTO::getReportDate).collect(Collectors.toList());
            biReportVO.setXAxisName("日期");
            biReportVO.setXAxis(xAxis);
            // 构造纵坐标数据
            List<WrapDataVO> yAxis = Lists.newArrayList();
            yAxis.add(buildWrapDataVO("组别", sortedData, ZhonganOutboundCallReportDTO::getDimension, FormatType.DEFAULT));
            yAxis.add(buildWrapDataVO("实际外呼量", sortedData, ZhonganOutboundCallReportDTO::getActualOutboundNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("接通量", sortedData, ZhonganOutboundCallReportDTO::getThroughputNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("通话总时长(分钟)", sortedData, ZhonganOutboundCallReportDTO::getDurationTotal, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("短信触发量", sortedData, ZhonganOutboundCallReportDTO::getSmsTriggersNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("短信成功发送量", sortedData, ZhonganOutboundCallReportDTO::getSmsSucSendNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("接通率", sortedData, ZhonganOutboundCallReportDTO::getContinuityRatio, FormatType.PERCENT_SCALE2));
            yAxis.add(buildWrapDataVO("接通短信触发率", sortedData, ZhonganOutboundCallReportDTO::getSmsTriggerRatio, FormatType.PERCENT_SCALE2));
            yAxis.add(buildWrapDataVO("短信成功发送率", sortedData, ZhonganOutboundCallReportDTO::getSmsSucSendRatio, FormatType.PERCENT_SCALE2));
            yAxis.add(buildWrapDataVO("成本", sortedData, ZhonganOutboundCallReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
            biReportVO.setYAxis(yAxis);
            biReportVOList.add(biReportVO);
        }
        return biReportVOList;
    }

    private void checkEmpty(Map<String, List<ZhonganOutboundCallReportDTO>> scoreMap) {
        if(scoreMap.isEmpty()){
            scoreMap.put("1",new ArrayList<>());
            scoreMap.put("7",new ArrayList<>());
            scoreMap.put("8",new ArrayList<>());
        }
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
        for (BiReportDownLoadParam param : params) {
            String name = param.getReportName() +"_"+ "场景" + param.getGroup();
            // excel sheet名称最大长度31，超出31截取前31位
            String sheetName =
                    name.length() > 31 ? name.substring(0, 31) : name;
            excelWriter.setSheet(sheetName);
            // 数据写入
            List<BiReportDownLoadParam> list = Lists.newArrayList();
            list.add(param);
            writeData(excelWriter, list);
        }
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
                        writer.writeCellValue(i + 1, j + 1, decimal.divide(BigDecimal.valueOf(100), decimal.scale() + 2, RoundingMode.HALF_UP));
                        writer.getCell(i + 1, j + 1).setCellStyle(getDataBarCellStyle(writer,decimal));
                    } else {
                        writer.writeCellValue(i + 1, j + 1, value);
                    }
                    writer.writeCellValue(i + 1, rowIndex + j + 1, value);
                }
            }
            // 添加空行 xAxis.size() + 1 为当前表格所占行数，再+1添加空行
            rowIndex += xAxis.size() + 2;
        }
        autoSizeColumnAll(writer);
    }

}
