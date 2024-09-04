package com.br.marketing.bi.xiecheng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengDataRatioDailyReportDTO;
import com.br.marketing.entity.DwsXcDataRatioD;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.XieChengBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 携程数据使用率报表适配实现
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_DATARATIO_DAILY_REPORT)
public class XieChengDataRatioDailyConverter extends AbstractBiReportConverter<BiReportVO, XiechengDataRatioDailyReportDTO> {
    @Autowired
    private XieChengBiReportMapper xieChengBiReportMapper;

    /**
     * 获取数据
     * @param param 查询条件
     * @return {@link List }<{@link XiechengDataRatioDailyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengDataRatioDailyReportDTO> fetchData(BiReportParam param) {
        List<XiechengDataRatioDailyReportDTO> dtos = Lists.newArrayList();
        // 近30天数据列表
        String reportDateStart = LocalDate.now().minusDays(30).toString();
        List<DwsXcDataRatioD> dwsXcDataRatioDS = xieChengBiReportMapper.selectXcDataRatioListbI_(reportDateStart);
        for (DwsXcDataRatioD dwsXcDataRatioD : dwsXcDataRatioDS) {
            long collidingBackNumber = dwsXcDataRatioD.getCollidingBackNum();
            long extractionNumber = dwsXcDataRatioD.getExtractionNum();
            long callableNumber = dwsXcDataRatioD.getCallableNum();
            BigDecimal extractionRatioOrg = (BigDecimal) dwsXcDataRatioD.getExtractionRatio();
            BigDecimal callableRatioOrg = (BigDecimal) dwsXcDataRatioD.getCallableRatio();

            BigDecimal extractionRatio = extractionRatioOrg.multiply(new BigDecimal(100)).setScale(3, RoundingMode.FLOOR);
            BigDecimal callableRatio = callableRatioOrg.multiply(new BigDecimal(100)).setScale(3, RoundingMode.FLOOR);
            // 创建DailyReportDTO实例并设置数据
            XiechengDataRatioDailyReportDTO report = new XiechengDataRatioDailyReportDTO();
            report.setReportDate(dwsXcDataRatioD.getReportDate());
            report.setCollidingBackNumber(collidingBackNumber);
            report.setExtractionNumber(extractionNumber);
            report.setCallableNumber(callableNumber);
            report.setExtractionRatio(extractionRatio);
            report.setCallableRatio(callableRatio);
            dtos.add(report);
        }
        return dtos;
    }

    /**
     * 数据处理
     * @param dtos   数据
     * @param extend 延长
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public BiReportVO process(List<XiechengDataRatioDailyReportDTO> dtos, JSONObject extend) {
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_DATARATIO_DAILY_REPORT.getTypeName());
        biReportVO.setReportName("数据使用率表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengDataRatioDailyReportDTO> sortedData = dtos.stream().sorted(Comparator.comparing(XiechengDataRatioDailyReportDTO::getReportDate
                , Comparator.naturalOrder())).collect(Collectors.toList());
        //构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengDataRatioDailyReportDTO::getReportDate).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        //构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        WrapDataVO collidingBackData = new WrapDataVO();
        collidingBackData.setName("撞得量");
        collidingBackData.setData(sortedData.stream().map(dto -> String.format(Locale.getDefault(), "%,d", dto.getCollidingBackNumber())).collect(Collectors.toList()));
        yAxis.add(collidingBackData);

        WrapDataVO extractionData = new WrapDataVO();
        extractionData.setName("析出量");
        extractionData.setData(sortedData.stream().map(dto -> String.format(Locale.getDefault(), "%,d", dto.getExtractionNumber())).collect(Collectors.toList()));
        yAxis.add(extractionData);

        WrapDataVO callableData = new WrapDataVO();
        callableData.setName("可外呼量");
        callableData.setData(sortedData.stream().map(dto -> String.format(Locale.getDefault(), "%,d", dto.getCallableNumber())).collect(Collectors.toList()));
        yAxis.add(callableData);

        WrapDataVO extractionRatioData = new WrapDataVO();
        extractionRatioData.setName("析出率");
        extractionRatioData.setData(sortedData.stream().map(dto -> dto.getExtractionRatio() + "%").collect(Collectors.toList()));
        yAxis.add(extractionRatioData);

        WrapDataVO callableRatioData = new WrapDataVO();
        callableRatioData.setName("可外呼率");
        callableRatioData.setData(sortedData.stream().map(dto -> dto.getCallableRatio() + "%").collect(Collectors.toList()));
        yAxis.add(callableRatioData);

        biReportVO.setYAxis(yAxis);
        return biReportVO;
    }


}
