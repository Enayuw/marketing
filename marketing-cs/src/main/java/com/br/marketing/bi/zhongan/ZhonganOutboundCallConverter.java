package com.br.marketing.bi.zhongan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.report.zhongan.ZhonganOutboundCallReportDTO;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.api.client.util.Lists;

import lombok.extern.slf4j.Slf4j;

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
        if (StringUtils.isEmpty(userType)) {
            return new ArrayList<>();
        }
        List<Integer> userTypes = new ArrayList<>();
        if (userType.contains(",")) {
            userTypes = Arrays.stream(userType.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        } else {
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
}
