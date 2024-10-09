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
import com.br.marketing.entity.ReportTask;
import com.br.marketing.enums.report.BiReportChartTypeEnum;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.GROUP_SCORE_REPORT)
public class ZhongAnGroupConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnGroupedScoreDistributionDTO> {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;
    @Autowired
    ReportTaskMapper reportTaskMapper;
    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public List<ZhongAnGroupedScoreDistributionDTO> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");
        if (StringUtils.isEmpty(taskId)) {
            return new ArrayList<>();
        }
        // 查询规则分组名称
        Map<String, String> groupNameMap = formatReportRules(taskId);

        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        reportStatisticTransferExample.createCriteria().andReportTaskIdEqualTo(taskId);
        reportStatisticTransferExample.setOrderByClause("create_time desc");
        List<ReportStatisticTransfer> reportStatisticTransfers = reportStatisticTransferMapper.selectByExample(reportStatisticTransferExample);
        if (reportStatisticTransfers.isEmpty()) {
            return new ArrayList<>();
        }

        ReportStatisticTransfer reportStatisticTransfer = reportStatisticTransfers.get(0);
        String reportId = reportStatisticTransfer.getReportId();
        String scoreField = reportStatisticTransfer.getScoreField();
        List<ScoreFieldDTO> scoreFieldDTOS = JSON.parseObject(scoreField, new TypeReference<List<ScoreFieldDTO>>() {
        }.getType());
        List<ZhongAnGroupedScoreDistributionDTO> dtos = new ArrayList<>();
        for (ScoreFieldDTO scoreFieldDTO : scoreFieldDTOS) {
            String dimensionField = reportStatisticTransfer.getDimensionField();
            String dimensionValue = reportStatisticTransfer.getDimensionValue();
            String field = scoreFieldDTO.getField();
            Integer step = scoreFieldDTO.getStep();
            // 分组查询
            for (String value : formatField(dimensionValue)) {
                List<ZhongAnDistributionStatisticDTO> dtoList = zhongAnBiReportMapper.selectZaMultiHeadGroupListbI_(reportId, field, dimensionField, value, "案件量");
                bulidGroupHead(dtoList, field, groupNameMap.get(value), step, dtos);
            }
        }
        return dtos;
    }

    private void bulidGroupHead(List<ZhongAnDistributionStatisticDTO> dtoList, String field, String groupName, Integer step, List<ZhongAnGroupedScoreDistributionDTO> dtos) {
        List<ZhongAnGroupedScoreDistributionDTO> list = new ArrayList<>();
        for (ZhongAnDistributionStatisticDTO zhongAnDistributionStatisticDTO : dtoList) {
            ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
            zhongAnGroupedScoreDistributionDTO.setProduct(field);
            zhongAnGroupedScoreDistributionDTO.setInterval(zhongAnDistributionStatisticDTO.getScoreValue());
            zhongAnGroupedScoreDistributionDTO.setGroup(groupName == null ? "0":groupName);
            zhongAnGroupedScoreDistributionDTO.setName(zhongAnDistributionStatisticDTO.getItemName());
            zhongAnGroupedScoreDistributionDTO.setNum(Long.valueOf(zhongAnDistributionStatisticDTO.getItemValue()));
            zhongAnGroupedScoreDistributionDTO.setStep(step);
            list.add(zhongAnGroupedScoreDistributionDTO);
        }
        fillProportion(list,ZhongAnGroupedScoreDistributionDTO::getNum,ZhongAnGroupedScoreDistributionDTO::setProportion);
        dtos.addAll(list);
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnGroupedScoreDistributionDTO> dtos, JSONObject extend) {
        List<BiReportVO> biReportVOS = Lists.newArrayList();

        Map<String, Map<String, List<ZhongAnGroupedScoreDistributionDTO>>> groupedByProductAndGroupName =
                dtos.stream().collect(Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getProduct,
                        Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getGroup)
                ));

        List<String> fiftyStepLengthList = marketingCommonConfig.getBiReportStepConfig().get("fiftyStepLength");
        List<String> fiveStepLengthList = marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");

        for (Map.Entry<String, Map<String, List<ZhongAnGroupedScoreDistributionDTO>>> entry1 : groupedByProductAndGroupName.entrySet()) {
            for (Map.Entry<String, List<ZhongAnGroupedScoreDistributionDTO>> entry : entry1.getValue().entrySet()) {
                // Y轴数据
                List<WrapDataVO> yAxisData = Lists.newArrayList();
                Map<String, List<ZhongAnGroupedScoreDistributionDTO>> comparisonMap = entry.getValue().stream()
                        .collect(Collectors.groupingBy(ZhongAnGroupedScoreDistributionDTO::getName));

                // X轴数据
                List<String> intervals = new ArrayList<>();
                Integer step;
                String group = "";
                // 处理数据
                for (String comparisonName : comparisonMap.keySet()) {
                    List<ZhongAnGroupedScoreDistributionDTO> comparisonData = comparisonMap.get(comparisonName);
                    step = comparisonData.get(0).getStep();
                    intervals.clear();
                    if(step == 50){
                        intervals.addAll(fiftyStepLengthList);
                    }else {
                        intervals.addAll(fiveStepLengthList);
                    }
                    group = comparisonData.get(0).getGroup();
                    List<ZhongAnGroupedScoreDistributionDTO> list = new ArrayList<>();
                    Long sum = 0L;
                    for (String interval : intervals){
                        int size = comparisonData.size();
                        int num = 1;
                        for (ZhongAnGroupedScoreDistributionDTO dto : comparisonData) {
                            if(interval.equals(dto.getInterval())){
                                sum += dto.getNum();
                                list.add(dto);
                                break;
                            }else if(size == num){
                                ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
                                zhongAnGroupedScoreDistributionDTO.setProduct(dto.getProduct());
                                zhongAnGroupedScoreDistributionDTO.setInterval(interval);
                                zhongAnGroupedScoreDistributionDTO.setName(dto.getName());
                                zhongAnGroupedScoreDistributionDTO.setNum(0L);
                                zhongAnGroupedScoreDistributionDTO.setProportion(new BigDecimal("0"));
                                zhongAnGroupedScoreDistributionDTO.setStep(dto.getStep());
                                list.add(zhongAnGroupedScoreDistributionDTO);
                            }else {
                                num ++;
                            }
                        }
                    }
                    // 占比字段格式化
                    List<ZhongAnGroupedScoreDistributionDTO> transformedList = list.stream()
                            .map(dto -> {
                                BigDecimal newProportion = dto.getProportion().multiply(new BigDecimal("100"));
                                dto.setProportion(newProportion.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros());
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // 增加总计
                    ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
                    zhongAnGroupedScoreDistributionDTO.setNum(sum);
                    zhongAnGroupedScoreDistributionDTO.setProportion(BigDecimal.valueOf(100));
                    transformedList.add(zhongAnGroupedScoreDistributionDTO);

                    yAxisData.add(buildWrapDataVO(comparisonName + "量级", transformedList, ZhongAnGroupedScoreDistributionDTO::getNum, FormatType.THOUSAND_SEPARATOR));
                    yAxisData.add(buildWrapDataVO(comparisonName + "占比", transformedList, ZhongAnGroupedScoreDistributionDTO::getProportion, FormatType.PERCENT_SIGN));
                }
                BiReportVO biReportVO = new BiReportVO();
                biReportVO.setReportName(entry1.getKey());
                biReportVO.setReportTypeName(BiReportTypeEnum.GROUP_SCORE_REPORT.getTypeName());
                biReportVO.setType(BiReportChartTypeEnum.TABLE.getType());
                biReportVO.setXAxisName("区间");
                biReportVO.setGroup(group);
                if(!intervals.contains("总计")){
                    intervals.add("总计");
                }
                biReportVO.setXAxis(intervals);
                biReportVO.setYAxis(yAxisData);
                biReportVOS.add(biReportVO);
            }
        }
        return biReportVOS;
    }


    public Map<String, String> formatReportRules(String taskId) {
        ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(Long.valueOf(taskId));
        String reportRules = reportTask.getReportRules();

        Map<String, String> resultMap = new HashMap<>();
        if (reportRules.contains("upload")) {
            JSONObject param = JSONObject.parseObject(reportRules);
            String upload = param.getString("upload");
            JSONObject uploadJson = JSONObject.parseObject(upload);
            String dimensions = uploadJson.getString("dimensionsValue");

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 解析JSON数组为JsonNode
                JsonNode jsonNode = objectMapper.readTree(dimensions);
                resultMap = new HashMap<>();
                for (JsonNode item : jsonNode) {
                    String code = item.get("code").asText();
                    String desc = item.get("desc").asText();
                    resultMap.put(code, desc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }


    public List<String> formatField(String str) {
        return JSON.parseObject(str, new TypeReference<List<String>>() {
        }.getType());
    }
}
