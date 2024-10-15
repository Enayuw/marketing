package com.br.marketing.bi.xiecheng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengDataRatioDailyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.proxy.XiechengBiReportService;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 携程数据使用率报表适配实现
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_DATARATIO_DAILY_REPORT)
public class XiechengDataRatioDailyConverter extends AbstractBiReportConverter<BiReportVO, XiechengDataRatioDailyReportDTO> {
    @Resource
    private XiechengBiReportService xiechengBiReportService;

    /**
     * 获取数据
     * @param param 查询条件
     * @return {@link List }<{@link XiechengDataRatioDailyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengDataRatioDailyReportDTO> fetchData(BiReportParam param) {
        // 近30天数据列表
        String reportDateStart = LocalDate.now().minusDays(30).toString();
        return xiechengBiReportService.selectXcDataRatioList(reportDateStart);
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
    public List<BiReportVO> process(List<XiechengDataRatioDailyReportDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_DATARATIO_DAILY_REPORT.getTypeName());
        biReportVO.setReportName("数据使用率表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengDataRatioDailyReportDTO> sortedData = dtos.stream()
                .sorted(Comparator.comparing(XiechengDataRatioDailyReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengDataRatioDailyReportDTO::getReportDate).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("撞得量", sortedData, XiechengDataRatioDailyReportDTO::getCollidingBackNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("析出量", sortedData, XiechengDataRatioDailyReportDTO::getExtractionNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("可外呼量", sortedData, XiechengDataRatioDailyReportDTO::getCallableNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("析出率", sortedData, XiechengDataRatioDailyReportDTO::getExtractionRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("可外呼率", sortedData, XiechengDataRatioDailyReportDTO::getCallableRatio, FormatType.PERCENT_SIGN));
        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }

}
