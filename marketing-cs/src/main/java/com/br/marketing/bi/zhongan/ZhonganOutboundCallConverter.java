package com.br.marketing.bi.zhongan;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhongAnGroupedScoreDistributionDTO;
import com.br.marketing.dto.report.zhongan.ZhonganOutboundCallReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


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
        String reportDateStart = condition.getString("startDate");
        String reportDateEnd = condition.getString("endDate");
        String userType = condition.getString("userType");
        if(StringUtils.isEmpty(userType)){
            return new ArrayList<>();
        }
        List<Integer> userTypes = new ArrayList<>();
        if(userType.contains(",")){
            userTypes = Arrays.stream(userType.split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }else {
            userTypes.add(Integer.valueOf(userType));
        }
        return zhongAnBiReportMapper.selectZaOutboundCallListbI_(reportDateStart, reportDateEnd, userTypes);
    }

    @Override
    public List<BiReportVO> process(List<ZhonganOutboundCallReportDTO> dtos, JSONObject extend) {

        Map<String, List<ZhonganOutboundCallReportDTO>> scoreMap =
                dtos.stream().collect(Collectors.groupingBy(ZhonganOutboundCallReportDTO::getUserType));

        List<BiReportVO> biReportVOList = Lists.newArrayList();
        for (Map.Entry<String, List<ZhonganOutboundCallReportDTO>> entry : scoreMap.entrySet()) {
            BiReportVO biReportVO = new BiReportVO();
            biReportVO.setReportTypeName(BiReportTypeEnum.OUTBOUND_STAT_REPORT.getTypeName());
            biReportVO.setReportName("外呼统计报表报表");
            biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
            biReportVO.setGroup(entry.getValue().get(0).getUserType());
            // 根据时间排序
            List<ZhonganOutboundCallReportDTO> sortedData = entry.getValue().stream()
                    .sorted(Comparator.comparing(ZhonganOutboundCallReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());
            // 构造横坐标数据
            List<String> xAxis = sortedData.stream().map(ZhonganOutboundCallReportDTO::getReportDate).distinct().collect(Collectors.toList());
            biReportVO.setXAxisName("日期");
            biReportVO.setXAxis(xAxis);
            // 构造纵坐标数据
            List<WrapDataVO> yAxis = Lists.newArrayList();
            yAxis.add(buildWrapDataVO("实际外呼量", sortedData, ZhonganOutboundCallReportDTO::getActualOutboundNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("接通量", sortedData, ZhonganOutboundCallReportDTO::getThroughputNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("通话总时长(分钟)", sortedData, ZhonganOutboundCallReportDTO::getDurationTotal, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("短信触发量", sortedData, ZhonganOutboundCallReportDTO::getSmsTriggersNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("短信成功发送量", sortedData, ZhonganOutboundCallReportDTO::getSmsSucSendNum, FormatType.THOUSAND_SEPARATOR));
            yAxis.add(buildWrapDataVO("接通率", sortedData, ZhonganOutboundCallReportDTO::getContinuityRatio, FormatType.PERCENT_SIGN));
            yAxis.add(buildWrapDataVO("接通短信触发率", sortedData, ZhonganOutboundCallReportDTO::getSmsTriggerRatio, FormatType.PERCENT_SIGN));
            yAxis.add(buildWrapDataVO("短信成功发送率", sortedData, ZhonganOutboundCallReportDTO::getSmsSucSendRatio, FormatType.PERCENT_SIGN));
            yAxis.add(buildWrapDataVO("成本", sortedData, ZhonganOutboundCallReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
            biReportVO.setYAxis(yAxis);
            biReportVOList.add(biReportVO);
        }
        return biReportVOList;
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
                    writer.writeCellValue(j, rowIndex + i + 1, Objects.equals("null", tags.get(j)) ? "NULL" : tags.get(j));
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
                    // 求和时处理千分位
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
