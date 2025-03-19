package com.br.marketing.bi.xiecheng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengTransferMonthlyReportDTO;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private XiechengBiReportService xiechengBiReportService;

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
        JSONObject condition = param.getCondition();
        String month = condition.getString("month");
        return xiechengBiReportService.selectXcTrabsferMonthlyList(month);
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
    public List<BiReportVO> process(List<XiechengTransferMonthlyReportDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_MONTHLY_REPORT.getTypeName());
        biReportVO.setReportName("月转化报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferMonthlyReportDTO> sortedData =
            dtos.stream().sorted(Comparator.comparing(XiechengTransferMonthlyReportDTO::getReportDate, Comparator.naturalOrder())).distinct()
                .collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferMonthlyReportDTO::getReportDate).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("锁定名单量", sortedData, XiechengTransferMonthlyReportDTO::getLockNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("上报名单量", sortedData, XiechengTransferMonthlyReportDTO::getSubmitNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("实际外呼量", sortedData, XiechengTransferMonthlyReportDTO::getOutboundNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("累计运营量", sortedData, XiechengTransferMonthlyReportDTO::getOperateNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("累计登录量", sortedData, XiechengTransferMonthlyReportDTO::getLoginNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证量", sortedData, XiechengTransferMonthlyReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请量", sortedData, XiechengTransferMonthlyReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("授信量", sortedData, XiechengTransferMonthlyReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现量", sortedData, XiechengTransferMonthlyReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现成功量", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawSucNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("日均授信量", sortedData, XiechengTransferMonthlyReportDTO::getCreditAvgNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录率", sortedData, XiechengTransferMonthlyReportDTO::getLoginRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证率", sortedData, XiechengTransferMonthlyReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请率", sortedData, XiechengTransferMonthlyReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信率", sortedData, XiechengTransferMonthlyReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请提现率", sortedData, XiechengTransferMonthlyReportDTO::getApplyWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请身份认证率", sortedData, XiechengTransferMonthlyReportDTO::getApplyCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证完成率", sortedData, XiechengTransferMonthlyReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("过件率", sortedData, XiechengTransferMonthlyReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现发起率", sortedData, XiechengTransferMonthlyReportDTO::getCreditWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现成功率", sortedData, XiechengTransferMonthlyReportDTO::getCreditWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请提现量2", sortedData, XiechengTransferMonthlyReportDTO::getApplyCreditNum2, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现成功量2", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawSucNum2, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现率2", sortedData, XiechengTransferMonthlyReportDTO::getApplyWithdrawRatio2, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率2", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawRatio2, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现发起率", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现成功率", sortedData, XiechengTransferMonthlyReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("总收入", sortedData, XiechengTransferMonthlyReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("总成本", sortedData, XiechengTransferMonthlyReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("ROI", sortedData, XiechengTransferMonthlyReportDTO::getRoi, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("授信目标完成率", sortedData, XiechengTransferMonthlyReportDTO::getCreditCompleteRatio, FormatType.PERCENT_SIGN));
        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }
}
