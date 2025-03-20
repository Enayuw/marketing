package com.br.marketing.bi.xiecheng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.xiecheng.XiechengTransferWeeklyReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.proxy.XiechengBiReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
import java.util.Map;
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

    @Resource
    private XiechengBiReportService xiechengBiReportService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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
        Map<String, Integer> xiechengBiReportShowNumMap = marketingCommonConfig.getXiechengBiReportShowNumMap();
        Integer sevenRollCount = xiechengBiReportShowNumMap.getOrDefault("sevenRollCount", 8);
        String reportDateStart = LocalDate.now().minusDays(sevenRollCount).toString();
        String reportDateEnd = LocalDate.now().minusDays(1).toString();
        return xiechengBiReportService.selectXcTransferSevenRollList(reportDateStart, reportDateEnd);
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
    public List<BiReportVO> process(List<XiechengTransferWeeklyReportDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOList = Lists.newArrayList();
        BiReportVO biReportVO = new BiReportVO();
        biReportVO.setReportTypeName(BiReportTypeEnum.XIECHENG_TRANSFER_WEEKLY_REPORT.getTypeName());
        biReportVO.setReportName("7日滚动转化报表");
        biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
        // 根据时间排序
        List<XiechengTransferWeeklyReportDTO> sortedData =
            dtos.stream().sorted(Comparator.comparing(XiechengTransferWeeklyReportDTO::getRollPeriod, Comparator.naturalOrder())).distinct()
                .collect(Collectors.toList());
        // 构造横坐标数据
        List<String> xAxis = sortedData.stream().map(XiechengTransferWeeklyReportDTO::getRollPeriod).collect(Collectors.toList());
        biReportVO.setXAxisName("日期");
        biReportVO.setXAxis(xAxis);
        // 构造纵坐标数据
        List<WrapDataVO> yAxis = Lists.newArrayList();
        yAxis.add(buildWrapDataVO("实际外呼量", sortedData, XiechengTransferWeeklyReportDTO::getOutboundNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录量", sortedData, XiechengTransferWeeklyReportDTO::getLoginNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("身份认证量", sortedData, XiechengTransferWeeklyReportDTO::getCertifyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请量", sortedData, XiechengTransferWeeklyReportDTO::getApplyNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("授信量", sortedData, XiechengTransferWeeklyReportDTO::getCreditNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("申请提现量", sortedData, XiechengTransferWeeklyReportDTO::getApplyWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("提现量", sortedData, XiechengTransferWeeklyReportDTO::getWithdrawNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("期均授信量", sortedData, XiechengTransferWeeklyReportDTO::getCreditAvgNum, FormatType.THOUSAND_SEPARATOR));
        yAxis.add(buildWrapDataVO("登录率", sortedData, XiechengTransferWeeklyReportDTO::getLoginRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证率", sortedData, XiechengTransferWeeklyReportDTO::getCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请率", sortedData, XiechengTransferWeeklyReportDTO::getApplyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("授信率", sortedData, XiechengTransferWeeklyReportDTO::getCreditRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现率", sortedData, XiechengTransferWeeklyReportDTO::getWithdrawRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("申请身份认证率", sortedData, XiechengTransferWeeklyReportDTO::getApplyCertifyRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("身份认证完成率", sortedData, XiechengTransferWeeklyReportDTO::getCertifyCompleteRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("过件率", sortedData, XiechengTransferWeeklyReportDTO::getOverPieceRatio, FormatType.PERCENT_SIGN));
        yAxis.add(buildWrapDataVO("提现成功率（授信后提现）", sortedData, XiechengTransferWeeklyReportDTO::getWithdrawSucRatio, FormatType.PERCENT_SIGN));
        biReportVO.setYAxis(yAxis);
        biReportVOList.add(biReportVO);
        return biReportVOList;
    }
}
