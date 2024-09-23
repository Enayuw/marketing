package com.br.marketing.bi.zhongan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.ScoreFieldDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnDistributionStatisticDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnGroupedScoreDistributionDTO;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ZhonganMultiHeadGroupConverter
 * @Description 多头分布报表适配实现
 * @Author kongbx
 * @Date 2024/9/21 15:25
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.MULTPOINT_REPORT)
public class ZhonganMultiHeadGroupConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnGroupedScoreDistributionDTO> {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;
    @Autowired
    ReportTaskMapper reportTaskMapper;
    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Override
    public List<ZhongAnGroupedScoreDistributionDTO> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");

        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        reportStatisticTransferExample.createCriteria().andReportTaskIdEqualTo(taskId);
        List<ReportStatisticTransfer> reportStatisticTransfers = reportStatisticTransferMapper.selectByExample(reportStatisticTransferExample);
        if(reportStatisticTransfers.isEmpty()){
            return new ArrayList<>();
        }

        ReportStatisticTransfer reportStatisticTransfer = reportStatisticTransfers.get(0);
        String reportId = reportStatisticTransfer.getReportId();
        String scoreField = reportStatisticTransfer.getScoreField();
        List<ScoreFieldDTO> scoreFieldDTOS = JSON.parseObject(scoreField, new TypeReference<List<ScoreFieldDTO>>() {}.getType());

        List<ZhongAnGroupedScoreDistributionDTO> dtos = new ArrayList<>();
        for (ScoreFieldDTO scoreFieldDTO : scoreFieldDTOS){
            String dimensionField = reportStatisticTransfer.getDimensionField();
            String dimensionValue = reportStatisticTransfer.getDimensionValue();
            String multiHeadField = reportStatisticTransfer.getMultiHeadField();
            String field = scoreFieldDTO.getField();
            Integer step = scoreFieldDTO.getStep();

            if(StringUtils.isNotEmpty(dimensionValue) && StringUtils.isNotEmpty(dimensionField)){
                // 分组多头查询
                for (String value : formatField(dimensionValue)) {
                    for (String itemName : formatField(multiHeadField)) {
                        List<ZhongAnDistributionStatisticDTO> dtoList = zhongAnBiReportMapper.selectZaMultiHeadGroupListbI_(reportId, field, dimensionField, value, itemName);
                        bulidMultiHead(dtoList,field,step,dtos);
                    }
                }
            }else if(StringUtils.isNotEmpty(multiHeadField)){
                // 多头查询
                for (String itemName : formatField(multiHeadField)) {
                    List<ZhongAnDistributionStatisticDTO> dtoList = zhongAnBiReportMapper.selectZaMultiHeadGroupListbI_(reportId, field, null, null, itemName);
                    bulidMultiHead(dtoList,field,step,dtos);
                }
            }
        }
        return dtos;
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnGroupedScoreDistributionDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOS = Lists.newArrayList();
        //先根据产品分组，一个分一个报表
        Map<String, List<ZhongAnGroupedScoreDistributionDTO>> scoreMap =
                dtos.stream().collect(Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getProduct));
        for (Map.Entry<String, List<ZhongAnGroupedScoreDistributionDTO>> entry : scoreMap.entrySet()) {
            BiReportVO biReportVO = new BiReportVO();
            biReportVO.setReportName(entry.getKey());
            biReportVO.setReportTypeName(BiReportTypeEnum.MULTPOINT_REPORT.getTypeName());
            biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
            // X轴数据
            List<String> intervals = entry.getValue().stream()
                    .map(ZhongAnGroupedScoreDistributionDTO::getInterval) // 将每个DTO的interval映射出来
                    .collect(Collectors.toList());
            biReportVO.setXAxisName("区间");
            biReportVO.setXAxis(intervals);
            // Y轴数据
            List<WrapDataVO> yAxisData = Lists.newArrayList();
            Map<String, List<ZhongAnGroupedScoreDistributionDTO>> comparisonMap = entry.getValue().stream()
                    .collect(Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getName));
            for (String comparisonName : comparisonMap.keySet()) {
                List<ZhongAnGroupedScoreDistributionDTO> comparisonData = comparisonMap.get(comparisonName);
                yAxisData.add(buildWrapDataVO(comparisonName + "量级", comparisonData, ZhongAnGroupedScoreDistributionDTO::getNum, FormatType.THOUSAND_SEPARATOR));
                yAxisData.add(buildWrapDataVO(comparisonName + "占比", comparisonData, ZhongAnGroupedScoreDistributionDTO::getProportion, FormatType.PERCENT_SIGN));
            }
            biReportVO.setYAxis(yAxisData);
            biReportVOS.add(biReportVO);
        }
        return biReportVOS;
    }

    public List<String> formatField(String str){
        return JSON.parseObject(str, new TypeReference<List<String>>() {}.getType());
    }

    public void bulidMultiHead(List<ZhongAnDistributionStatisticDTO> dtoList, String field,
                               Integer step, List<ZhongAnGroupedScoreDistributionDTO> dtos) {
        for (ZhongAnDistributionStatisticDTO zhongAnDistributionStatisticDTO : dtoList) {
            ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
            zhongAnGroupedScoreDistributionDTO.setProduct(field);
            zhongAnGroupedScoreDistributionDTO.setInterval(zhongAnDistributionStatisticDTO.getScoreValue());
            zhongAnGroupedScoreDistributionDTO.setName(zhongAnDistributionStatisticDTO.getItemName());
            zhongAnGroupedScoreDistributionDTO.setNum(Long.valueOf(zhongAnDistributionStatisticDTO.getItemValue()));
            zhongAnGroupedScoreDistributionDTO.setStep(step);
            dtos.add(zhongAnGroupedScoreDistributionDTO);
        }
        fillProportion(dtos,ZhongAnGroupedScoreDistributionDTO::getNum,ZhongAnGroupedScoreDistributionDTO::setProportion);
    }

}
