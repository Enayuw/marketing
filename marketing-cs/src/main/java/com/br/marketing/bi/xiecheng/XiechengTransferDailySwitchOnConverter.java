package com.br.marketing.bi.xiecheng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengTransferDailyReportDTO;
import com.br.marketing.dto.report.xiecheng.XiechengTransferDailySwitchOnReportDTO;
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
 * 携程日接通转化报表适配实现
 * @author guangxiu.li
 * @date 2024/10/11 17:30
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.XIECHENG_TRANSFER_DAILYSWITCHON_REPORT)
public class XiechengTransferDailySwitchOnConverter extends AbstractBiReportConverter<BiReportVO, XiechengTransferDailySwitchOnReportDTO> {
    @Resource
    private XiechengBiReportService xiechengBiReportService;

    /**
     * 获取数据
     * @author guangxiu.li
     * @date 2024/10/11 17:30
     * @param param
     * @return java.util.List<com.br.marketing.dto.report.xiecheng.XiechengTransferDailySwitchOnReportDTO>
     */
    @Override
    public List<XiechengTransferDailySwitchOnReportDTO> fetchData(BiReportParam param) {
        String startDate = param.getCondition().getString("startDate");
        String endDate = param.getCondition().getString("endDate");
        return xiechengBiReportService.selectXcTransferDailySwitchOn(startDate, endDate);
    }

    /**
     * 数据处理
     * @author guangxiu.li
     * @date 2024/10/11 17:31
     * @param dtos
     * @param extend
     * @return com.br.marketing.vo.bi.BiReportVO
     */
    @Override
    public List<BiReportVO> process(List<XiechengTransferDailySwitchOnReportDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_DAILYSWITCHON_REPORT.getTypeName());
        biReportVO.setReportName("接通日转化报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferDailySwitchOnReportDTO> sortedData = dtos.stream()
            .sorted(Comparator.comparing(XiechengTransferDailySwitchOnReportDTO::getReportDate, Comparator.naturalOrder())).collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferDailySwitchOnReportDTO::getReportDate).distinct().collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("当日运营量", sortedData, XiechengTransferDailySwitchOnReportDTO::getOperateNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日接通量", sortedData, XiechengTransferDailySwitchOnReportDTO::getCallNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日登录量", sortedData, XiechengTransferDailySwitchOnReportDTO::getLoginNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日身份认证量", sortedData, XiechengTransferDailySwitchOnReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日申请量", sortedData, XiechengTransferDailySwitchOnReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日授信量", sortedData, XiechengTransferDailySwitchOnReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日申请提现", sortedData, XiechengTransferDailySwitchOnReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日提现量", sortedData, XiechengTransferDailySwitchOnReportDTO::getWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日登录率", sortedData, XiechengTransferDailySwitchOnReportDTO::getLoginRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日身份认证率", sortedData, XiechengTransferDailySwitchOnReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日申请率", sortedData, XiechengTransferDailySwitchOnReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日授信率", sortedData, XiechengTransferDailySwitchOnReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日申请提现率", sortedData, XiechengTransferDailySwitchOnReportDTO::getApplyWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日提现率", sortedData, XiechengTransferDailySwitchOnReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日申请身份认证率", sortedData, XiechengTransferDailySwitchOnReportDTO::getApplyCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日身份认证完成率", sortedData, XiechengTransferDailySwitchOnReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日过件率", sortedData, XiechengTransferDailySwitchOnReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日提现发起率", sortedData, XiechengTransferDailySwitchOnReportDTO::getWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日提现成功率", sortedData, XiechengTransferDailySwitchOnReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日收入", sortedData, XiechengTransferDailySwitchOnReportDTO::getIncome, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("当日成本", sortedData, XiechengTransferDailySwitchOnReportDTO::getCost, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("ROI", sortedData, XiechengTransferDailySwitchOnReportDTO::getRoi, FormatType.THOUSAND_SEPARATOR_DECIMAL));
        yAxis.add(buildWrapDataVO("当日授信后提现发起", sortedData, XiechengTransferDailySwitchOnReportDTO::getCreditWithdrawLaunchNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日授信后提现成功", sortedData, XiechengTransferDailySwitchOnReportDTO::getCreditWithdrawSucNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("当日授信后发起提现率", sortedData, XiechengTransferDailySwitchOnReportDTO::getCreditWithdrawLaunchRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("当日授信后提现成功率", sortedData, XiechengTransferDailySwitchOnReportDTO::getCreditWithdrawSucRatio, FormatType.PERCENT_SIGN));
        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }
}
