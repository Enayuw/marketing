package com.br.marketing.check.service.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.dto.ModelStatisticsData;
import com.br.marketing.check.enums.ModelTypeEnum;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.IntervalRangeDTO;
import com.br.marketing.entity.ReportStatisticsScore;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.service.Impl.CustomIntervalStatisticsImpl;
import com.br.marketing.service.Impl.FixedIntervalStatisticsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 自定义区间统计处理器
 * 负责处理自定义区间的统计逻辑
 *
 * @author bingxu.kong
 */
@Slf4j
@Component
public class CustomIntervalStatisticsProcessor {

    @Autowired
    private CustomIntervalStatisticsImpl customIntervalStatistics;

    @Autowired
    private FixedIntervalStatisticsImpl fixedIntervalStatistics;

    @Autowired
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    /**
     * 处理自定义区间统计
     */
    public void processStatistics(ModelStatisticsData data) {
        ReportStatisticsScore statisticsScore = data.getStatisticsScore();
        
        try {
            if (data.getModelTypeEnum() == ModelTypeEnum.SINGLE_MODEL) {
                // 普通单模型使用自定义区间统计
                processSingleModelStatistics(statisticsScore);
            } else if (data.getModelTypeEnum() == ModelTypeEnum.IMAGE_MODEL) {
                // 画像模型使用固定区间的简单分组统计逻辑
                processImageModelStatistics(statisticsScore);
            } else if (data.getModelTypeEnum() == ModelTypeEnum.MULTI_MODEL) {
                processMultiModelStatistics(statisticsScore);
            }
            
            // 更新统计状态为成功
            updateReportScore(statisticsScore, 1, null);
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "自定义区间统计异常"), e);
            updateReportScore(statisticsScore, 2, "自定义区间统计异常");
        }
    }

    /**
     * 处理单模型自定义区间统计
     */
    private void processSingleModelStatistics(ReportStatisticsScore statisticsScore) {
        String fieldXRange = statisticsScore.getFieldXRange();
        List<IntervalRangeDTO> xIntervalList = JSON.parseArray(fieldXRange, IntervalRangeDTO.class);
        List<String> batchNumberList = customIntervalStatistics.getBatchNumberKey(statisticsScore);
        
        customIntervalStatistics.executeCustomIntervalCount(
                statisticsScore.getId(),
                statisticsScore.getFieldX(),
                null,
                batchNumberList,
                xIntervalList,
                null,
                "单模型");
    }

    /**
     * 处理画像模型统计（借鉴FixedIntervalStatisticsProcessor的逻辑）
     */
    private void processImageModelStatistics(ReportStatisticsScore statisticsScore) {
        List<String> modelList = Arrays.asList(statisticsScore.getFieldX().split(","));
        
        modelList.forEach(model -> {
            String batchNumbers = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(model);
            if (StringUtils.isEmpty(batchNumbers)) {
                return;
            }
            
            // 画像模型使用简单分组统计，不使用自定义区间
            List<Map<String, Object>> results = fixedIntervalStatistics.imageModelCount(model, batchNumbers);
            
            // 直接保存画像模型结果，不补充空区间
            fixedIntervalStatistics.saveImageModelResults(statisticsScore.getId(), results, model);
            
            log.warn("自定义区间处理器-画像模型统计完成，模型: {}, 结果数: {}", model, results.size());
        });
    }

    /**
     * 处理多模型自定义区间统计
     */
    private void processMultiModelStatistics(ReportStatisticsScore statisticsScore) {
        String fieldXRange = statisticsScore.getFieldXRange();
        String fieldYRange = statisticsScore.getFieldYRange();
        
        List<IntervalRangeDTO> xIntervalList = JSON.parseArray(fieldXRange, IntervalRangeDTO.class);
        List<IntervalRangeDTO> yIntervalList = JSON.parseArray(fieldYRange, IntervalRangeDTO.class);
        List<String> batchNumberList = customIntervalStatistics.getBatchNumberKey(statisticsScore);
        
        customIntervalStatistics.executeCustomIntervalCount(
                statisticsScore.getId(),
                statisticsScore.getFieldX(),
                statisticsScore.getFieldY(),
                batchNumberList,
                xIntervalList,
                yIntervalList,
                "多模型");
    }

    /**
     * 更新报表统计分数状态
     */
    private void updateReportScore(ReportStatisticsScore statisticsScore, Integer status, String errorDesc) {
        statisticsScore.setStatus(status);
        statisticsScore.setUpdateTime(new Date());
        if (errorDesc != null) {
            statisticsScore.setStatisticsDesc(errorDesc);
        }
        reportStatisticsScoreMapper.updateByPrimaryKeySelective(statisticsScore);
    }
}