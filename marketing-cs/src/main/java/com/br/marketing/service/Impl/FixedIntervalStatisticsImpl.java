package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.ScoreStatisticsDetail;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.mapper.ScoreStatisticsDetailMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 固定区间统计实现类
 * 负责处理固定区间的统计逻辑
 *
 * @author bingxu.kong
 */
@Slf4j
@Service
public class FixedIntervalStatisticsImpl {

    private final static String TITLE = "【固定区间统计】";

    @Autowired
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Autowired
    private ScoreStatisticsDetailMapper scoreStatisticsDetailMapper;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 单模型统计
     */
    public List<Map<String, Object>> singleModelCount(String fieldX, String batchNumbers, String fieldXRange) {
        List<String> batchNumberList = Arrays.asList(batchNumbers.split(","));
        StringBuilder scoreSqlBuilder = new StringBuilder();
        
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSqlBuilder.append("select ").append(fieldX).append(" as num from b_score_").append(batchNumberList.get(i));
            } else {
                scoreSqlBuilder.append("select ").append(fieldX).append(" as num from b_score_").append(batchNumberList.get(i)).append(" union all ");
            }
        }
        
        String scoreSql = "SELECT " +
                "concat('[',FLOOR(a.num / xModelRange) * xModelRange,',',FLOOR(a.num/xModelRange) * xModelRange + xModelRange,')')" +
                "AS " + fieldX + ",count(1) AS num FROM (" + scoreSqlBuilder + " ) a GROUP BY FLOOR(a.num / xModelRange)" +
                " ORDER BY FLOOR(a.num / xModelRange);";
        
        // 替换变量
        scoreSql = scoreSql.replace("xModelRange", fieldXRange);
        
        log.warn(TITLE + "单模型={} 统计sql={}", fieldX, scoreSql);
        return reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);
    }

    /**
     * 多模型统计
     */
    public List<Map<String, Object>> multiModelCount(String fieldX, String fieldY, String batchNumbers, 
                                                     String fieldXRange, String fieldYRange) {
        List<String> batchNumberList = Arrays.asList(batchNumbers.split(","));
        StringBuilder scoreSqlBuilder = new StringBuilder();
        
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSqlBuilder.append("select ").append(fieldX).append(",").append(fieldY)
                        .append(" from b_score_").append(batchNumberList.get(i));
            } else {
                scoreSqlBuilder.append("select ").append(fieldX).append(",").append(fieldY)
                        .append(" from b_score_").append(batchNumberList.get(i)).append(" union all ");
            }
        }
        
        String scoreSql = "SELECT " +
                "concat('[',FLOOR(a." + fieldX + " / xModelRange) * xModelRange,',',FLOOR(a." + fieldX + "/xModelRange) * xModelRange + xModelRange,')')" +
                "AS " + fieldX + "," +
                "concat('[',FLOOR(a." + fieldY + " / yModelRange) * yModelRange,',',FLOOR(a." + fieldY + "/yModelRange) * yModelRange + yModelRange,')')" +
                "AS " + fieldY + ",count(1) AS num FROM (" + scoreSqlBuilder + " ) a " +
                "GROUP BY FLOOR(a." + fieldX + " / xModelRange), FLOOR(a." + fieldY + " / yModelRange) " +
                "ORDER BY FLOOR(a." + fieldX + " / xModelRange), FLOOR(a." + fieldY + " / yModelRange);";
        
        // 替换变量
        scoreSql = scoreSql.replace("xModelRange", fieldXRange).replace("yModelRange", fieldYRange);
        
        log.warn(TITLE + "多模型={},{} 统计sql={}", fieldX, fieldY, scoreSql);
        return reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);
    }

    /**
     * 画像模型统计
     */
    public List<Map<String, Object>> imageModelCount(String fieldX, String batchNumbers) {
        List<String> batchNumberList = Arrays.asList(batchNumbers.split(","));
        StringBuilder scoreSqlBuilder = new StringBuilder();
        
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSqlBuilder.append("select ").append(fieldX).append(" from b_score_").append(batchNumberList.get(i));
            } else {
                scoreSqlBuilder.append("select ").append(fieldX).append(" from b_score_").append(batchNumberList.get(i)).append(" union all ");
            }
        }

        // 画像模型使用简单分组统计
        String scoreSql = "SELECT " + fieldX + ", count(1) AS num FROM (" + scoreSqlBuilder + " ) a GROUP BY " + fieldX + ";";
        
        log.warn(TITLE + "画像模型={} 统计sql={}", fieldX, scoreSql);
        return reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);
    }

    /**
     * 根据步长获取预定义的区间配置
     */
    public List<String> getPredefinedIntervals(Integer stepLength) {
        if (stepLength == null) {
            log.warn(TITLE + "步长为空，使用默认5步长区间");
            return marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");
        }
        if (stepLength.equals(5)) {
            return marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");
        } else if (stepLength.equals(50)) {
            return marketingCommonConfig.getBiReportStepConfig().get("fiftyStepLength");
        } else if (stepLength.equals(10)) {
            return marketingCommonConfig.getBiReportStepConfig().get("tenStepLength");
        } else {
            log.warn(TITLE + "未支持的步长: {}，使用默认5步长区间", stepLength);
            return marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");
        }
    }

    /**
     * 保存固定区间统计结果（单模型）
     */
    public void saveFixedIntervalResults(Long statisticsId, List<Map<String, Object>> results, 
                                       String model, List<String> predefinedIntervals) {
        List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
        
        // 创建所有预定义区间的记录，初始count为0
        for (String interval : predefinedIntervals) {
            ScoreStatisticsDetail detail = new ScoreStatisticsDetail();
            detail.setStatisticsId(statisticsId);
            detail.setFieldXValue(interval);
            detail.setFieldYValue(model);
            detail.setFieldNum(0);
            detail.setIsDel(Constants.DATA_VALID);
            statisticsDetails.add(detail);
        }
        
        // 更新有实际数据的区间count
        for (Map<String, Object> resultMap : results) {
            String intervalValue = (String) resultMap.get(model);
            Object numObj = resultMap.get("num");
            
            // 空值检查
            if (intervalValue == null) {
                log.warn(TITLE + "空区间赋值记录，model: {}, intervalValue: {}, num: {}", model, intervalValue, numObj);
                intervalValue = "[-1,0)";
            }

            Integer count = 0;
            // 空值检查
            if (numObj != null) {
                count = ((Long) numObj).intValue();
            }
            
            for (ScoreStatisticsDetail detail : statisticsDetails) {
                if (intervalValue.equals(detail.getFieldXValue())) {
                    detail.setFieldNum(count);
                    break;
                }
            }
        }
        
        // 批量插入
        for (ScoreStatisticsDetail detail : statisticsDetails) {
            scoreStatisticsDetailMapper.insertSelective(detail);
        }
        
        log.warn(TITLE + "保存单模型固定区间结果完成，model: {}, statisticsId: {}, 结果数: {}",
                model, statisticsId, statisticsDetails.size());
    }

    /**
     * 保存固定区间统计结果（包含所有预定义区间，即使count为0）
     * 用于多模型固定区间统计
     */
    public void saveFixedIntervalResults(Long statisticsId, List<Map<String, Object>> results,
                                         String fieldX, String fieldY,
                                         List<String> xPredefinedIntervals, List<String> yPredefinedIntervals) {
        // 从SQL查询结果中获取实际有数据的统计
        Map<String, Map<String, Integer>> actualResults = new HashMap<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (Map<String, Object> resultMap : results) {
                String xValue = (String) resultMap.get(fieldX);
                String yValue = (String) resultMap.get(fieldY);
                Integer count = ((Long) resultMap.get("num")).intValue();

                // 处理空值情况：如果x或y有null的情况，默认将该区间置为[-1,0)
                if (xValue == null) {
                    log.warn(TITLE + "X字段空值赋值记录，field: {}, xValue: {}, yValue: {}, num: {}", 
                            fieldX, xValue, yValue, count);
                    xValue = "[-1,0)";
                }
                if (yValue == null) {
                    log.warn(TITLE + "Y字段空值赋值记录，field: {}, xValue: {}, yValue: {}, num: {}", 
                            fieldY, xValue, yValue, count);
                    yValue = "[-1,0)";
                }

                actualResults.computeIfAbsent(xValue, k -> new HashMap<>()).put(yValue, count);
            }
        }

        // 根据预定义区间生成完整的统计结果
        List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
        for (String xIntervalText : xPredefinedIntervals) {
            for (String yIntervalText : yPredefinedIntervals) {
                // 获取实际统计结果，如果没有则为0
                Integer count = actualResults.getOrDefault(xIntervalText, new HashMap<>())
                        .getOrDefault(yIntervalText, 0);

                ScoreStatisticsDetail statisticsDetail = createStatisticsDetail(
                        statisticsId, xIntervalText, yIntervalText, count);
                statisticsDetails.add(statisticsDetail);
            }
        }

        // 批量插入结果
        if (!CollectionUtils.isEmpty(statisticsDetails)) {
            scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
        }

        log.warn("生成固定区间结果完成（多模型），statisticsId: {}, X预定义区间数: {}, Y预定义区间数: {}, 有数据区间数: {}",
                statisticsId, xPredefinedIntervals.size(), yPredefinedIntervals.size(), actualResults.size());
    }

    /**
     * 创建统计详情对象
     */
    private ScoreStatisticsDetail createStatisticsDetail(Long statisticsId, String xValue, String yValue, Integer count) {
        ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
        statisticsDetail.setStatisticsId(statisticsId);
        statisticsDetail.setFieldXValue(xValue);
        statisticsDetail.setFieldYValue(yValue);
        statisticsDetail.setFieldNum(count);
        statisticsDetail.setCreateTime(new Date());
        statisticsDetail.setUpdateTime(new Date());
        return statisticsDetail;
    }

    /**
     * 保存画像模型结果（直接保存，不补充空区间）
     */
    public void saveImageModelResults(Long statisticsId, List<Map<String, Object>> results, String model) {
        List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(results)) {
            for (Map<String, Object> resultMap : results) {
                Object modelValueObj = resultMap.get(model);
                Object numObj = resultMap.get("num");
                // 空值检查
                Integer count = 0;
                // 空值检查
                if (numObj != null) {
                    count = ((Long) numObj).intValue();
                }

                ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
                statisticsDetail.setStatisticsId(statisticsId);
                // 安全的字符串转换
                String modelValue = (modelValueObj == null || StringUtils.isEmpty(modelValueObj.toString()))
                    ? "未知" : modelValueObj.toString();
                statisticsDetail.setFieldXValue(modelValue);
                statisticsDetail.setFieldYValue(model);

                statisticsDetail.setFieldNum(count);
                statisticsDetails.add(statisticsDetail);
            }
        }

        // 批量插入
        if (!CollectionUtils.isEmpty(statisticsDetails)) {
            scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
        }

        log.warn(TITLE + "保存画像模型结果完成，model: {}, statisticsId: {}, 结果数: {}",
                model, statisticsId, statisticsDetails.size());
    }

}