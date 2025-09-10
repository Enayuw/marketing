package com.br.marketing.check.service.processor;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.dto.ModelStatisticsData;
import com.br.marketing.check.enums.ModelTypeEnum;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.Impl.FixedIntervalStatisticsImpl;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ReportStatisticsScore;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 固定区间统计处理器
 * 负责处理固定区间的统计逻辑
 *
 * @author bingxu.kong
 */
@Slf4j
@Component
public class FixedIntervalStatisticsProcessor {

    @Autowired
    private FixedIntervalStatisticsImpl fixedIntervalStatistics;

    @Autowired
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    /**
     * 处理固定区间统计
     */
    public void processStatistics(ModelStatisticsData data) {
        ReportStatisticsScore statisticsScore = data.getStatisticsScore();
        String imageDistribution = data.getImageDistribution();
        
        try {
            if (data.getModelTypeEnum() == ModelTypeEnum.SINGLE_MODEL || data.getModelTypeEnum() == ModelTypeEnum.IMAGE_MODEL) {
                processSingleModelStatistics(statisticsScore, imageDistribution);
            } else if (data.getModelTypeEnum() == ModelTypeEnum.MULTI_MODEL) {
                processMultiModelStatistics(statisticsScore);
            }
            
            // 更新统计状态为成功
            updateReportScore(statisticsScore, 1, null);
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "固定区间统计异常"), e);
            updateReportScore(statisticsScore, 2, "固定区间统计异常");
        }
    }

    /**
     * 处理单模型统计（包括画像模型）
     */
    private void processSingleModelStatistics(ReportStatisticsScore statisticsScore, String imageDistribution) {
        List<String> modelList = Arrays.asList(statisticsScore.getFieldX().split(","));
        String fieldXRange = statisticsScore.getFieldXRange();
        
        modelList.forEach(model -> {
            String batchNumbers = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(model);
            if (StringUtils.isEmpty(batchNumbers)) {
                return;
            }
            
            List<Map<String, Object>> results;
            
            // 判断是否为画像模型
            boolean isImageModel = imageDistribution.contains(model) && !"pd_id_apply_age".equals(model);
            
            if (isImageModel) {
                // 画像模型统计
                results = fixedIntervalStatistics.imageModelCount(model, batchNumbers);
                saveImageModelResults(statisticsScore.getId(), results, model);
            } else {
                // 普通单模型统计
                results = fixedIntervalStatistics.singleModelCount(model, batchNumbers, fieldXRange);
                List<String> predefinedIntervals = fixedIntervalStatistics.getPredefinedIntervals(Integer.valueOf(fieldXRange));
                fixedIntervalStatistics.saveFixedIntervalResults(statisticsScore.getId(), results, model, predefinedIntervals);
            }
        });
    }

    /**
     * 处理多模型统计
     */
    private void processMultiModelStatistics(ReportStatisticsScore statisticsScore) {
        String xModel = statisticsScore.getFieldX();
        String yModel = statisticsScore.getFieldY();
        String modelKey = xModel.concat("_").concat(yModel);
        String batchNumbers = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(modelKey);
        
        if (StringUtils.isEmpty(batchNumbers)) {
            return;
        }
        
        String fieldXRange = statisticsScore.getFieldXRange();
        String fieldYRange = statisticsScore.getFieldYRange();
        
        List<Map<String, Object>> results = fixedIntervalStatistics.multiModelCount(
                xModel, yModel, batchNumbers, fieldXRange, fieldYRange);
        
        List<String> xPredefinedIntervals = fixedIntervalStatistics.getPredefinedIntervals(Integer.valueOf(fieldXRange));
        List<String> yPredefinedIntervals = fixedIntervalStatistics.getPredefinedIntervals(Integer.valueOf(fieldYRange));
        
        fixedIntervalStatistics.saveFixedIntervalResults(
                statisticsScore.getId(), results, xModel, yModel, xPredefinedIntervals, yPredefinedIntervals);
    }

    /**
     * 保存画像模型结果
     */
    private void saveImageModelResults(Long statisticsId, List<Map<String, Object>> results, String model) {
        fixedIntervalStatistics.saveImageModelResults(statisticsId, results, model);
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