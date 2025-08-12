package com.br.marketing.check.service.processor;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.check.dto.ModelStatisticsData;
import com.br.marketing.check.enums.ModelTypeEnum;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.report.IntervalRangeDTO;
import com.br.marketing.entity.ReportStatisticsScore;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.service.Impl.CustomIntervalStatisticsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

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
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    /**
     * 处理自定义区间统计
     */
    public void processStatistics(ModelStatisticsData data) {
        ReportStatisticsScore statisticsScore = data.getStatisticsScore();
        
        try {
            if (data.getModelTypeEnum() == ModelTypeEnum.SINGLE_MODEL || data.getModelTypeEnum() == ModelTypeEnum.IMAGE_MODEL) {
                processSingleModelStatistics(statisticsScore);
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