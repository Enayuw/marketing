package com.br.marketing.bi.xiecheng;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengTransferWeeklyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 携程7日滚动转化报表实现
 *
 * @author senyang.zheng
 * @date 2024/09/04
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_TRANSFER_WEEKLY_REPORT)
public class XiechengTransferWeeklyConverter extends AbstractBiReportConverter<BiReportVO, XiechengTransferWeeklyReportDTO> {
    /**
     * 获取数据
     *
     * @param param 参数
     * @return {@link List }<{@link XiechengTransferWeeklyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengTransferWeeklyReportDTO> fetchData(BiReportParam param) {
        List<XiechengTransferWeeklyReportDTO> dtos = Lists.newArrayList();
        // 创建Random实例
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            // 创建DailyReportDTO实例并设置数据
            XiechengTransferWeeklyReportDTO report = new XiechengTransferWeeklyReportDTO();
            report.setRollPeriod(DateUtil.formatDate(DateUtil.offsetDay(new Date(), -i)));
            report.setOutboundNum((long) random.nextInt(5000000));
            report.setCertifyNum((long) random.nextInt(5000000));
            report.setApplyNum((long) random.nextInt(5000000));
            report.setCreditNum((long) random.nextInt(5000000));
            report.setApplyWithdrawNum((long) random.nextInt(5000000));
            report.setWithdrawNum((long) random.nextInt(5000000));
            report.setCreditAvgNum((long) random.nextInt(5000000));
            report.setCertifyRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setApplyRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setCreditRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setWithdrawRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setCertifyCompleteRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setOverPieceRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setWithdrawSucRatio(new BigDecimal(random.nextInt(100))
                    .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            dtos.add(report);
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
    public BiReportVO process(List<XiechengTransferWeeklyReportDTO> dtos, JSONObject extend) {
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_MONTHLY_REPORT.getTypeName());
        biReportVO.setReportName("数据使用率表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferWeeklyReportDTO> sortedData = dtos.stream().sorted(Comparator.comparing(XiechengTransferWeeklyReportDTO::getRollPeriod
                , Comparator.naturalOrder())).collect(Collectors.toList());
        //构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferWeeklyReportDTO::getRollPeriod).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        //构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("实际外呼量级", sortedData, XiechengTransferWeeklyReportDTO::getOutboundNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证量", sortedData, XiechengTransferWeeklyReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请量", sortedData, XiechengTransferWeeklyReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("授信量", sortedData, XiechengTransferWeeklyReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现量", sortedData, XiechengTransferWeeklyReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现量", sortedData, XiechengTransferWeeklyReportDTO::getWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("期均授信量", sortedData, XiechengTransferWeeklyReportDTO::getCreditAvgNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证率", sortedData, XiechengTransferWeeklyReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请率", sortedData, XiechengTransferWeeklyReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信率", sortedData, XiechengTransferWeeklyReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率", sortedData, XiechengTransferWeeklyReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证完成率", sortedData, XiechengTransferWeeklyReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("过件率", sortedData, XiechengTransferWeeklyReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现成功率（授信后提现）", sortedData, XiechengTransferWeeklyReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        biReportVO.setYAxis(yAxis);
        return biReportVO;
    }
}
