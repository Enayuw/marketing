package com.br.marketing.bi.xiecheng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengTransferMonthlySwitchOnReportDTO;
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
 * 携程月接通转化报表适配实现
 * @author guangxiu.li
 * @date 2024/10/10 16:58
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_TRANSFER_MONTHLYSWITCHON_REPORT)
public class XiechengTransferMonthlySwitchOnConverter extends AbstractBiReportConverter<BiReportVO, XiechengTransferMonthlySwitchOnReportDTO> {

    @Resource
    private XiechengBiReportService xiechengBiReportService;

    /**
     * 获取数据
     * @author guangxiu.li
     * @date 2024/10/11 17:20
     * @param param
     * @return java.util.List<com.br.marketing.dto.report.xiecheng.XiechengTransferMonthlySwitchOnReportDTO>
     */
    @Override
    public List<XiechengTransferMonthlySwitchOnReportDTO> fetchData(BiReportParam param) {
        JSONObject condition = param.getCondition();
        String month = condition.getString("month");
        return xiechengBiReportService.selectXcTrabsferMonthlySwitchOnList(month);
    }

    /**
     * 数据处理
     * @author guangxiu.li
     * @date 2024/10/11 17:20
     * @param dtos
     * @param extend
     * @return com.br.marketing.vo.bi.BiReportVO
     */
    @Override
    public List<BiReportVO> process(List<XiechengTransferMonthlySwitchOnReportDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_MONTHLYSWITCHON_REPORT.getTypeName());
        biReportVO.setReportName("接通月转化报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferMonthlySwitchOnReportDTO> sortedData =
            dtos.stream().sorted(Comparator.comparing(XiechengTransferMonthlySwitchOnReportDTO::getReportDate, Comparator.naturalOrder())).distinct()
                .collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferMonthlySwitchOnReportDTO::getReportDate).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("锁定名单量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getLockNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("上报名单量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getSubmitNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("实际外呼量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getOutboundNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("累计运营量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getOperateNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("累计接通量级（未去重）", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCallNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("累计接通量级（去重）", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getDistinctCallNum,
                FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getLoginNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("授信量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现成功量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getWithdrawSucNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("日均授信量", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCreditAvgNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getLoginRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请提现率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请身份认证率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证完成率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("过件率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现发起率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCreditWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信后提现成功率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCreditWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请提现量2", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyCreditNum2,
                FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现成功量2", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getWithdrawSucNum2, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现率2", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getApplyWithdrawRatio2, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率2", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getWithdrawRatio2, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现发起率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现成功率", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("总收入", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("总成本", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("ROI", sortedData, XiechengTransferMonthlySwitchOnReportDTO::getRoi, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }
}
