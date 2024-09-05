package com.br.marketing.bi.xiecheng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengTransferDailyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程日转化报表适配实现
 *
 * @author senyang.zheng
 * @date 2024/09/04
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_TRANSFER_DAILY_REPORT)
public class XiechengTransferDailyConverter extends AbstractBiReportConverter<BiReportVO, XiechengTransferDailyReportDTO> {

    /**
     * 获取数据
     *
     * @param param 参数
     * @return {@link List }<{@link XiechengTransferDailyReportDTO }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    @Override
    public List<XiechengTransferDailyReportDTO> fetchData(BiReportParam param) {
        List<XiechengTransferDailyReportDTO> dtos = Lists.newArrayList();
        // 创建Random实例
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            // 创建DailyReportDTO实例并设置数据
            XiechengTransferDailyReportDTO report = new XiechengTransferDailyReportDTO();
            report.setReportDate(DateUtil.formatDate(DateUtil.offsetDay(new Date(), -i)));
            report.setOperateNum((long)random.nextInt(5000000));
            report.setCertifyNum((long)random.nextInt(5000000));
            report.setApplyNum((long)random.nextInt(5000000));
            report.setCreditNum((long)random.nextInt(5000000));
            report.setApplyWithdrawNum((long)random.nextInt(5000000));
            report.setWithdrawNum((long)random.nextInt(5000000));
            report.setCertifyRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setApplyRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setCreditRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setApplyWithdrawRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setWithdrawRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setCertifyCompleteRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setOverPieceRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setWithdrawLaunchRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setWithdrawSucRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setIncome(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setCost(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setRoi(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setCreditWithdrawLaunchNum((long)random.nextInt(5000000));
            report.setCreditWithdrawSucNum((long)random.nextInt(5000000));
            report.setCreditWithdrawLaunchRatio(new BigDecimal(random.nextInt(100))
                .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setCreditWithdrawSucRatio(new BigDecimal(random.nextInt(100))
                .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setSubmitMillionTransferNum((long)random.nextInt(5000000));
            report.setOutboundMillionTransferNum((long)random.nextInt(5000000));
            dtos.add(report);
        }
        return dtos;
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
    public BiReportVO process(List<XiechengTransferDailyReportDTO> dtos, JSONObject extend) {
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_DAILY_REPORT.getTypeName());
        biReportVO.setReportName("日转化报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferDailyReportDTO> sortedData = dtos.stream()
            .sorted(Comparator.comparing(XiechengTransferDailyReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferDailyReportDTO::getReportDate).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("当日运营量级", sortedData, XiechengTransferDailyReportDTO::getOperateNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日身份认证量级", sortedData, XiechengTransferDailyReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日申请量级", sortedData, XiechengTransferDailyReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日授信量", sortedData, XiechengTransferDailyReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日申请提现", sortedData, XiechengTransferDailyReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日提现量", sortedData, XiechengTransferDailyReportDTO::getWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日身份认证率", sortedData, XiechengTransferDailyReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日申请率", sortedData, XiechengTransferDailyReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日授信率", sortedData, XiechengTransferDailyReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日申请提现率", sortedData, XiechengTransferDailyReportDTO::getApplyWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日提现率", sortedData, XiechengTransferDailyReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日身份认证完成率", sortedData, XiechengTransferDailyReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日过件率", sortedData, XiechengTransferDailyReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日提现发起率", sortedData, XiechengTransferDailyReportDTO::getWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日提现成功率", sortedData, XiechengTransferDailyReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日收入", sortedData, XiechengTransferDailyReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日成本", sortedData, XiechengTransferDailyReportDTO::getCost, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("ROI", sortedData, XiechengTransferDailyReportDTO::getRoi, FormatType.PERCENT_SIGN));
        yAxis
            .add(buildWrapDataVO("当日授信后提现发起", sortedData, XiechengTransferDailyReportDTO::getCreditWithdrawLaunchNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日授信后提现成功", sortedData, XiechengTransferDailyReportDTO::getCreditWithdrawSucNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日授信后提现发起率", sortedData, XiechengTransferDailyReportDTO::getCreditWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现成功率", sortedData, XiechengTransferDailyReportDTO::getCreditWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis
            .add(buildWrapDataVO("上报数据百万转化", sortedData, XiechengTransferDailyReportDTO::getSubmitMillionTransferNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(
            buildWrapDataVO("外呼数据百万转化", sortedData, XiechengTransferDailyReportDTO::getOutboundMillionTransferNum, FormatType.THOUSAND_SEPARATOR));
        biReportVO.setYAxis(yAxis);
        return biReportVO;
    }
}
