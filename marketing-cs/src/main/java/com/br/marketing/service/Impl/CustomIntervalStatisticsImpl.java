package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.IntervalRangeDTO;
import com.br.marketing.entity.ReportStatisticsScore;
import com.br.marketing.entity.ScoreStatisticsDetail;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.mapper.ScoreStatisticsDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * 自定义区间统计
 * 统一管理自定义区间相关的统计逻辑，避免重复代码
 *
 * @author bingxu.kong
 * @date 2025-08-07
 */
@Slf4j
@Component
public class CustomIntervalStatisticsImpl {

    @Resource
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Resource
    private ScoreStatisticsDetailMapper scoreStatisticsDetailMapper;

    /**
     * 执行自定义区间统计（统一的统计逻辑）
     *
     * @param statisticsId    统计ID
     * @param fieldX          X轴字段名
     * @param fieldY          Y轴字段名（单模型时为null）
     * @param batchNumberList 批次号列表
     * @param xIntervalList   X轴区间配置
     * @param yIntervalList   Y轴区间配置（单模型时为null）
     * @param logPrefix       日志前缀
     */
    public void executeCustomIntervalCount(Long statisticsId, String fieldX, String fieldY,
                                           List<String> batchNumberList,
                                           List<IntervalRangeDTO> xIntervalList,
                                           List<IntervalRangeDTO> yIntervalList,
                                           String logPrefix) {
        if (CollectionUtils.isEmpty(xIntervalList)) {
            log.warn("{}自定义区间统计x轴模型为空，statisticsId: {}", logPrefix, statisticsId);
            return;
        }

        if (fieldY == null) {
            // 单模型：按每个模型分别统计
            executeSingleModelCustomIntervalCount(statisticsId, fieldX, batchNumberList, xIntervalList, logPrefix);
        } else {
            // 多模型：统计模型组合
            executeMultiModelCustomIntervalCount(statisticsId, fieldX, fieldY, batchNumberList, xIntervalList, yIntervalList, logPrefix);
        }
    }

    /**
     * 执行单模型自定义区间统计
     */
    private void executeSingleModelCustomIntervalCount(Long statisticsId, String fieldX, 
                                                      List<String> batchNumberList,
                                                      List<IntervalRangeDTO> xIntervalList, 
                                                      String logPrefix) {
        // 单模型fieldX可能包含多个模型，需要分别统计每个模型
        String[] modelNames = fieldX.split(",");
        
        for (String modelName : modelNames) {
            String trimmedModelName = modelName.trim();
            
            // 为每个模型构建查询SQL
            String scoreSql = buildScoreSql(batchNumberList, trimmedModelName, null);
            
            // 构建自定义区间统计SQL
            String customIntervalSql = buildCustomIntervalSql(scoreSql, trimmedModelName, null, xIntervalList, null);
            
            log.warn("{}单模型自定义区间统计SQL，模型: {}, SQL: {}", logPrefix, trimmedModelName, customIntervalSql);
            
            List<Map<String, Object>> results = reportStatisticsScoreMapper.queryDataMapNumbI_(customIntervalSql);
            
            // 保存统计结果，field_y_value存储模型名称
            saveSingleModelCustomIntervalResults(statisticsId, results, trimmedModelName, xIntervalList);
        }
    }

    /**
     * 执行多模型自定义区间统计
     */
    private void executeMultiModelCustomIntervalCount(Long statisticsId, String fieldX, String fieldY,
                                                     List<String> batchNumberList,
                                                     List<IntervalRangeDTO> xIntervalList,
                                                     List<IntervalRangeDTO> yIntervalList,
                                                     String logPrefix) {
        if (CollectionUtils.isEmpty(yIntervalList)) {
            log.warn("{}多模型自定义区间统计y轴模型为空，statisticsId: {}", logPrefix, statisticsId);
            return;
        }

        // 构建查询SQL
        String scoreSql = buildScoreSql(batchNumberList, fieldX, fieldY);

        // 构建自定义区间统计SQL
        String customIntervalSql = buildCustomIntervalSql(scoreSql, fieldX, fieldY, xIntervalList, yIntervalList);

        log.warn("{}多模型自定义区间统计SQL: {}", logPrefix, customIntervalSql);

        List<Map<String, Object>> results = reportStatisticsScoreMapper.queryDataMapNumbI_(customIntervalSql);

        // 保存统计结果（包含所有定义的区间）
        saveCustomIntervalResults(statisticsId, results, fieldX, fieldY, xIntervalList, yIntervalList);
    }

    /**
     * 构建基础查询SQL
     */
    public String buildScoreSql(List<String> batchNumberList, String fieldX, String fieldY) {
        if (batchNumberList.size() == 1) {
            // 单表查询，不需要UNION ALL
            StringBuilder scoreSql = new StringBuilder();
            scoreSql.append("SELECT ").append(fieldX);
            if (fieldY != null) {
                scoreSql.append(", ").append(fieldY);
            }
            scoreSql.append(" FROM b_score_").append(batchNumberList.get(0));
            return scoreSql.toString();
        } else {
            // 多表查询，使用UNION ALL
            StringBuilder scoreSql = new StringBuilder();
            for (int i = 0; i < batchNumberList.size(); i++) {
                if (i > 0) {
                    scoreSql.append(" UNION ALL ");
                }
                scoreSql.append("SELECT ").append(fieldX);
                if (fieldY != null) {
                    scoreSql.append(", ").append(fieldY);
                }
                scoreSql.append(" FROM b_score_").append(batchNumberList.get(i));
            }
            return scoreSql.toString();
        }
    }

    /**
     * 构建自定义区间统计SQL
     */
    public String buildCustomIntervalSql(String baseSql, String fieldX, String fieldY,
                                         List<IntervalRangeDTO> xIntervalList, List<IntervalRangeDTO> yIntervalList) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH score_ranges AS (SELECT ").append(fieldX);
        if (fieldY != null) {
            sql.append(", ").append(fieldY);
        }

        // 构建X轴CASE WHEN
        sql.append(", CASE ");
        for (IntervalRangeDTO interval : xIntervalList) {
            // 如果是[-1,0)区间，先处理空值情况
            if ("[-1,0)".equals(interval.getText())) {
                sql.append("WHEN ").append(fieldX).append(" IS NULL THEN '").append(interval.getText()).append("' ");
            }else{
                sql.append("WHEN ").append(fieldX);
                if (interval.getMinInclusive()) {
                    sql.append(" >= ").append(interval.getMin());
                } else {
                    sql.append(" > ").append(interval.getMin());
                }
                sql.append(" AND ").append(fieldX);
                if (interval.getMaxInclusive()) {
                    sql.append(" <= ").append(interval.getMax());
                } else {
                    sql.append(" < ").append(interval.getMax());
                }
                sql.append(" THEN '").append(interval.getText()).append("' ");
            }
        }
        sql.append("ELSE 'OUT_OF_RANGE' END AS x_range");

        // 如果是多模型，构建Y轴CASE WHEN
        if (fieldY != null && !CollectionUtils.isEmpty(yIntervalList)) {
            sql.append(", CASE ");
            for (IntervalRangeDTO interval : yIntervalList) {
                // 如果是[-1,0)区间，先处理空值情况
                if ("[-1,0)".equals(interval.getText())) {
                    sql.append("WHEN ").append(fieldY).append(" IS NULL THEN '").append(interval.getText()).append("' ");
                }else {
                    sql.append("WHEN ").append(fieldY);
                    if (interval.getMinInclusive()) {
                        sql.append(" >= ").append(interval.getMin());
                    } else {
                        sql.append(" > ").append(interval.getMin());
                    }
                    sql.append(" AND ").append(fieldY);
                    if (interval.getMaxInclusive()) {
                        sql.append(" <= ").append(interval.getMax());
                    } else {
                        sql.append(" < ").append(interval.getMax());
                    }
                    sql.append(" THEN '").append(interval.getText()).append("' ");
                }
            }
            sql.append("ELSE 'OUT_OF_RANGE' END AS y_range");
        }

        sql.append(" FROM (").append(baseSql).append(") a");
        // 移除空值过滤条件，让空值也能被统计到[-1,0)区间中
        sql.append(") ");

        // 构建最终查询
        sql.append("SELECT x_range AS ").append(fieldX);
        if (fieldY != null) {
            sql.append(", y_range AS ").append(fieldY);
        }
        sql.append(", COUNT(1) AS num FROM score_ranges WHERE x_range != 'OUT_OF_RANGE'");
        if (fieldY != null) {
            sql.append(" AND y_range != 'OUT_OF_RANGE'");
        }
        sql.append(" GROUP BY x_range");
        if (fieldY != null) {
            sql.append(", y_range");
        }
        sql.append(" ORDER BY x_range");
        if (fieldY != null) {
            sql.append(", y_range");
        }

        return sql.toString();
    }

    /**
     * 保存单模型自定义区间统计结果（包含所有定义的区间）
     */
    private void saveSingleModelCustomIntervalResults(Long statisticsId, List<Map<String, Object>> results,
                                                     String modelName, List<IntervalRangeDTO> xIntervalList) {
        // 从SQL查询结果中获取实际有数据的统计
        Map<String, Integer> actualResults = new HashMap<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (Map<String, Object> resultMap : results) {
                String xValue = (String) resultMap.get(modelName);
                Integer count = ((Long) resultMap.get("num")).intValue();
                actualResults.put(xValue, count);
            }
        }

        // 生成完整的区间结果
        List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
        for (IntervalRangeDTO xInterval : xIntervalList) {
            String xIntervalText = xInterval.getText();
            Integer count = actualResults.getOrDefault(xIntervalText, 0);
            
            ScoreStatisticsDetail statisticsDetail = createStatisticsDetail(
                    statisticsId, xIntervalText, modelName, count);
            statisticsDetails.add(statisticsDetail);
        }

        if (!CollectionUtils.isEmpty(statisticsDetails)) {
            scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
        }

        log.warn("保存单模型自定义区间结果完成，statisticsId: {}, 模型: {}, 总区间数: {}, 有数据区间数: {}",
                statisticsId, modelName, statisticsDetails.size(), actualResults.size());
    }

    /**
     * 保存自定义区间统计结果（包含所有定义的区间，即使count为0）
     */
    public void saveCustomIntervalResults(Long statisticsId, List<Map<String, Object>> results,
                                          String fieldX, String fieldY,
                                          List<IntervalRangeDTO> xIntervalList,
                                          List<IntervalRangeDTO> yIntervalList) {
        // 从SQL查询结果中获取实际有数据的统计
        Map<String, Map<String, Integer>> actualResults = new HashMap<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (Map<String, Object> resultMap : results) {
                String xValue = (String) resultMap.get(fieldX);
                String yValue = fieldY != null ? (String) resultMap.get(fieldY) : fieldX;
                Integer count = ((Long) resultMap.get("num")).intValue();

                actualResults.computeIfAbsent(xValue, k -> new HashMap<>()).put(yValue, count);
            }
        }

        // 根据区间定义生成完整的统计结果（包括count为0的区间）
        List<ScoreStatisticsDetail> statisticsDetails = generateCompleteIntervalResults(
                statisticsId, actualResults, fieldX, fieldY, xIntervalList, yIntervalList);

        // 批量插入结果
        if (!CollectionUtils.isEmpty(statisticsDetails)) {
            scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
        }
    }

    /**
     * 生成完整的区间统计结果（包括count为0的区间）
     */
    private List<ScoreStatisticsDetail> generateCompleteIntervalResults(Long statisticsId,
                                                                        Map<String, Map<String, Integer>> actualResults,
                                                                        String fieldX, String fieldY,
                                                                        List<IntervalRangeDTO> xIntervalList,
                                                                        List<IntervalRangeDTO> yIntervalList) {
        List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();

        if (CollectionUtils.isEmpty(xIntervalList)) {
            log.warn("X轴区间配置为空，无法生成完整区间结果，statisticsId: {}", statisticsId);
            return statisticsDetails;
        }

        // 生成所有可能的区间组合
        if (fieldY != null && !CollectionUtils.isEmpty(yIntervalList)) {
            // 多模型：生成X轴和Y轴的笛卡尔积
            for (IntervalRangeDTO xInterval : xIntervalList) {
                String xIntervalText = xInterval.getText();

                for (IntervalRangeDTO yInterval : yIntervalList) {
                    String yIntervalText = yInterval.getText();

                    // 获取实际统计结果，如果没有则为0
                    Integer count = actualResults.getOrDefault(xIntervalText, new HashMap<>())
                            .getOrDefault(yIntervalText, 0);

                    ScoreStatisticsDetail statisticsDetail = createStatisticsDetail(
                            statisticsId, xIntervalText, yIntervalText, count);
                    statisticsDetails.add(statisticsDetail);
                }
            }
        }

        log.warn("生成完整区间结果完成，statisticsId: {}, 总区间数: {}, 有数据区间数: {}",
                statisticsId, statisticsDetails.size(), actualResults.size());

        return statisticsDetails;
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
     * 获取批次号列表
     * 
     * @param statisticsScore 统计分数对象
     * @return 批次号列表
     */
    public List<String> getBatchNumberKey(ReportStatisticsScore statisticsScore) {
        Integer reportScoreType = statisticsScore.getReportScoreType();
        String fieldX = statisticsScore.getFieldX();
        String fieldY = statisticsScore.getFieldY();
        String batchNumberListJson = statisticsScore.getBatchNumberList();

        Set<String> allBatchNumbers = new HashSet<>();
        JSONObject batchNumberJson = JSONObject.parseObject(batchNumberListJson);
        
        if (reportScoreType.equals(1)) {
            // 单模型：fieldX包含多个模型名，用逗号分隔
            // batchNumberListJson格式：{"scorencashonzawswyyym":"7410717_20250310000000_1894,7410717_20250310000000_1895","scorescashonyxxy":"7410717_20250310000000_1894"}
            String[] modelNames = fieldX.split(",");
            
            for (String modelName : modelNames) {
                String batchNumberStr = batchNumberJson.getString(modelName.trim());
                if (StringUtils.isNotEmpty(batchNumberStr)) {
                    // 处理每个key对应的批次号可能有多个的情况，用逗号分隔
                    String[] batchNumbers = batchNumberStr.split(",");
                    for (String batchNumber : batchNumbers) {
                        if (StringUtils.isNotEmpty(batchNumber.trim())) {
                            allBatchNumbers.add(batchNumber.trim());
                        }
                    }
                }
            }
        } else {
            // 多模型：使用fieldX_fieldY作为key
            // batchNumberListJson格式：{"scorescashonyxxy_scorefxsbbaseb":"7410717_20250310000000_1894,7410717_20250310000000_1895"}
            String batchNumberKey = fieldX.concat("_").concat(fieldY);
            String batchNumberStr = batchNumberJson.getString(batchNumberKey);
            
            if (StringUtils.isNotEmpty(batchNumberStr)) {
                // 处理批次号可能有多个的情况，用逗号分隔
                String[] batchNumbers = batchNumberStr.split(",");
                for (String batchNumber : batchNumbers) {
                    if (StringUtils.isNotEmpty(batchNumber.trim())) {
                        allBatchNumbers.add(batchNumber.trim());
                    }
                }
            }
        }
        
        List<String> result = new ArrayList<>(allBatchNumbers);
        log.warn("获取批次号列表，reportScoreType: {}, fieldX: {}, fieldY: {}, 批次数: {}",
                reportScoreType, fieldX, fieldY, result.size());
        
        return result;
    }
}