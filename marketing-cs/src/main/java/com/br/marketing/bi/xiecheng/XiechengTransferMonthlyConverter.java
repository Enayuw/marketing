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
import com.br.marketing.dto.report.xiecheng.XiechengTransferMonthlyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;

import cn.hutool.core.date.DateUtil;
import groovy.util.logging.Slf4j;

/**
 * 携程月转化报表适配实现
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_TRANSFER_MONTHLY_REPORT)
public class XiechengTransferMonthlyConverter extends AbstractBiReportConverter<BiReportVO, XiechengTransferMonthlyReportDTO> {

    /**
     * 获取数据
     *
     * @param param 参数
     * @return {@link List }<{@link XiechengTransferMonthlyReportDTO }>
     * @author senyang.zheng
     * @date 2024/09/04
     */
    @Override
    public List<XiechengTransferMonthlyReportDTO> fetchData(BiReportParam param) {
        List<XiechengTransferMonthlyReportDTO> dtos = Lists.newArrayList();
        // 创建Random实例
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            // 创建DailyReportDTO实例并设置数据
            XiechengTransferMonthlyReportDTO report = new XiechengTransferMonthlyReportDTO();
            report.setReportDate(DateUtil.formatDate(DateUtil.offsetDay(new Date(), -i)));
            report.setLockNum((long)random.nextInt(5000000));
            report.setSubmitNum((long)random.nextInt(5000000));
            report.setOutboundNum((long)random.nextInt(5000000));
            report.setOperateNum((long)random.nextInt(5000000));
            report.setCertifyNum((long)random.nextInt(5000000));
            report.setApplyNum((long)random.nextInt(5000000));
            report.setCreditNum((long)random.nextInt(5000000));
            report.setApplyWithdrawNum((long)random.nextInt(5000000));
            report.setWithdrawSucNum((long)random.nextInt(5000000));
            report.setCreditAvgNum((long)random.nextInt(5000000));
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
            report.setCreditWithdrawLaunchRatio(new BigDecimal(random.nextInt(100))
                .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setCreditWithdrawSucRatio(new BigDecimal(random.nextInt(100))
                .divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR).multiply(new BigDecimal(100)));
            report.setApplyCreditNum2((long)random.nextInt(5000000));
            report.setWithdrawSucNum2((long)random.nextInt(5000000));
            report.setApplyWithdrawRatio2(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
            report.setWithdrawRatio2(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
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
            report.setCreditCompleteRatio(new BigDecimal(random.nextInt(100)).divide(new BigDecimal(random.nextInt(100) + 1), 2, RoundingMode.FLOOR)
                .multiply(new BigDecimal(100)));
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
     * @date 2024/09/04
     */
    @Override
    public BiReportVO process(List<XiechengTransferMonthlyReportDTO> dtos, JSONObject extend) {
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_MONTHLY_REPORT.getTypeName());
        biReportVO.setReportName("月转化报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferMonthlyReportDTO> sortedData = dtos.stream()
            .sorted(Comparator.comparing(XiechengTransferMonthlyReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferMonthlyReportDTO::getReportDate).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("锁定名单量级", sortedData, XiechengTransferMonthlyReportDTO::getLockNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("上报名单量级", sortedData, XiechengTransferMonthlyReportDTO::getSubmitNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("实际外呼量级", sortedData, XiechengTransferMonthlyReportDTO::getOutboundNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("累计运营量级", sortedData, XiechengTransferMonthlyReportDTO::getOperateNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证量级", sortedData, XiechengTransferMonthlyReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请量", sortedData, XiechengTransferMonthlyReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("授信量", sortedData, XiechengTransferMonthlyReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现量", sortedData, XiechengTransferMonthlyReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现成功量", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawSucNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("日均授信量", sortedData, XiechengTransferMonthlyReportDTO::getCreditAvgNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证率", sortedData, XiechengTransferMonthlyReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请率", sortedData, XiechengTransferMonthlyReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信率", sortedData, XiechengTransferMonthlyReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请提现率", sortedData, XiechengTransferMonthlyReportDTO::getApplyWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证完成率", sortedData, XiechengTransferMonthlyReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("过件率", sortedData, XiechengTransferMonthlyReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现发起率", sortedData, XiechengTransferMonthlyReportDTO::getCreditWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现成功率", sortedData, XiechengTransferMonthlyReportDTO::getCreditWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请授信量级2", sortedData, XiechengTransferMonthlyReportDTO::getApplyCreditNum2, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现成功量级2", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawSucNum2, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现率2", sortedData, XiechengTransferMonthlyReportDTO::getApplyWithdrawRatio2, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率2", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawRatio2, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现发起率", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现成功率", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("总收入", sortedData, XiechengTransferMonthlyReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("总成本", sortedData, XiechengTransferMonthlyReportDTO::getCost, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("ROI", sortedData, XiechengTransferMonthlyReportDTO::getRoi, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信目标完成率", sortedData, XiechengTransferMonthlyReportDTO::getCreditCompleteRatio, FormatType.PERCENT_SIGN));
        biReportVO.setYAxis(yAxis);
        return biReportVO;
    }
}
