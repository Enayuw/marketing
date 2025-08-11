package com.br.marketing.check.service.Impl.scorereport;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.IntervalRangeDTO;
import com.br.marketing.dto.report.ScoreReportRuleDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.report.ReportTaskStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.Impl.CustomIntervalStatisticsImpl;
import com.br.marketing.service.bi.AnalysisReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * 跑分报表统计service
 *
 * @author zhen.Li1
 * @dateTime 2024/08/16 14:32
 */
@Slf4j
@Service
public class ScoreReportTaskServiceImpl implements ScoreReportTaskService {

    @Resource
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Resource
    private ScoreStatisticsDetailMapper scoreStatisticsDetailMapper;

    @Resource
    private ReportTaskMapper reportTaskMapper;


    @Resource
    private AnalysisReportService analysisReportService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ReportIntervalConfigMapper reportIntervalConfigMapper;

    @Resource
    private ReportIntervalModelMapper reportIntervalModelMapper;

    @Resource
    private CustomIntervalStatisticsImpl customIntervalStatistics;

    private final static String IMAGEMODEL = "pd_cell_type,pd_id_apply_age,pd_id_gender,pd_cell_province";


    @Override
    public void scoreReportCount(ReportTask reportTask) {
        // 检查是否使用自定义区间模板
        if (StringUtils.isNotEmpty(reportTask.getTemplateId())) {
            // 使用自定义区间统计
            customIntervalReportCount(reportTask);
        } else {
            // 使用原有固定区间统计
            //解析规则，生成报表
            reportRuleBuild(reportTask);
            //跑分统计计算
            reportRuleCount(reportTask);
        }
        //更新任务状态
        updateReportTask(reportTask);
    }

    /**
     * 更新报表任务
     *
     * @param reportTask
     * @return
     */
    private void updateReportTask(ReportTask reportTask) {
        ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
        statisticsScoreExample.createCriteria()
                .andReportIdEqualTo(reportTask.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);
        Long failNum = statisticsScoreList.stream().filter(reportStatisticsScore -> reportStatisticsScore.getStatus() != 1).count();
        reportTask.setStatus((statisticsScoreList.size() == 0 || failNum > 0) ? ReportTaskStatusEnum.FAIL.getValue() :
                ReportTaskStatusEnum.SUCCESS.getValue());
        reportTask.setUpdateTime(new Date());
        reportTask.setGroupCount(statisticsScoreList.size());
        reportTaskMapper.updateByPrimaryKey(reportTask);
        //上传至dfs
        try {
            analysisReportService.uploadReportToFastDfs(reportTask.getId());
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "跑分模型统计上传FastDfs异常"), e);
        }
    }


    /**
     * 统计任务统计
     *
     * @param reportTask
     * @return
     */
    private void reportRuleCount(ReportTask reportTask) {
        ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
        statisticsScoreExample.createCriteria()
                .andReportIdEqualTo(reportTask.getId())
                .andStatusIsNull()
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);
        if (CollectionUtils.isEmpty(statisticsScoreList)) {
            return;
        }
        statisticsScoreList.forEach((ReportStatisticsScore statisticsScore) -> {
            //单模型统计
            if (statisticsScore.getReportScoreType().equals(1)) {
                List<String> modelList = new ArrayList<>(Arrays.asList(statisticsScore.getFieldX().split(",")));
                try {
                    modelList.forEach((String model) -> {
                        String batchNumebrs = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(model);
                        List<Map<String, Object>> singleResult;
                        if(IMAGEMODEL.contains(model)){
                            singleResult = imageModelCount(model, batchNumebrs, statisticsScore.getFieldXRange());
                        }else {
                            singleResult = singleModelCount(model, batchNumebrs, statisticsScore.getFieldXRange());
                        }

                        // 根据模型类型选择不同的保存策略
                        if(IMAGEMODEL.contains(model)){
                            // 画像模型特殊处理
                            saveImageModelResults(statisticsScore.getId(), singleResult, model);
                        } else {
                            // 普通模型使用预定义区间配置
                            List<String> predefinedIntervals = getPredefinedIntervals(Integer.valueOf(statisticsScore.getFieldXRange()));
                            customIntervalStatistics.saveFixedIntervalResults(
                                    statisticsScore.getId(), singleResult, model, model, predefinedIntervals);
                        }
                    });
                    updateReportScore(statisticsScore, 1, null);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "跑分模型统计异常"), e);
                    updateReportScore(statisticsScore, 2, "跑分模型统计异常");
                }
            } else {
                //多模型统计
                String batchNumebrs = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(statisticsScore.getFieldX().concat("_")
                        .concat(statisticsScore.getFieldY()));
                try {
                    List<Map<String, Object>> mulResult = mulModelCount(statisticsScore.getFieldX(), statisticsScore.getFieldY(),
                            batchNumebrs, statisticsScore.getFieldXRange(), statisticsScore.getFieldYRange());
                    
                    // 根据fieldXRange和fieldYRange获取预定义的区间配置
                    List<String> xPredefinedIntervals = getPredefinedIntervals(Integer.valueOf(statisticsScore.getFieldXRange()));
                    List<String> yPredefinedIntervals = getPredefinedIntervals(Integer.valueOf(statisticsScore.getFieldYRange()));
                    
                    // 使用新的保存方法，包含所有预定义区间（包括count为0的）
                    customIntervalStatistics.saveFixedIntervalResults(
                            statisticsScore.getId(), mulResult, 
                            statisticsScore.getFieldX(), statisticsScore.getFieldY(), 
                            xPredefinedIntervals, yPredefinedIntervals);
                    
                    updateReportScore(statisticsScore, 1, null);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "跑分模型统计异常"), e);
                    updateReportScore(statisticsScore, 2, "跑分模型统计异常");
                }
            }
        });
    }


    private void updateReportScore(ReportStatisticsScore statisticsScore, Integer status, String errorDesc) {
        statisticsScore.setStatus(status);
        statisticsScore.setUpdateTime(new Date());
        statisticsScore.setStatisticsDesc(errorDesc);
        reportStatisticsScoreMapper.updateByPrimaryKey(statisticsScore);
    }


    /**
     * 多模型任务统计计算
     *
     * @param fieldX
     * @param fieldY
     * @param batchNumebrs
     * @param fieldYRange
     * @param fieldXRange
     * @return List
     */
    private List<Map<String, Object>> mulModelCount(String fieldX, String fieldY, String batchNumebrs, String fieldXRange, String fieldYRange) {

        List<String> batchNumberList = Arrays.asList(batchNumebrs.split(","));

        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(",").concat(fieldY).concat(" from b_score_")
                        .concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(",").concat(fieldY).concat(" from b_score_")
                        .concat(batchNumberList.get(i)).concat(" union all ");
            }

        }
        scoreSql = "SELECT " +
                "concat('[',FLOOR(a.xModelName/xModelRange) * xModelRange,',',FLOOR(a.xModelName/xModelRange) * xModelRange + xModelRange,')')" +
                "AS xModelName,concat('[',FLOOR(a.yModelName/yModelRange) * yModelRange,',',FLOOR(a.yModelName/yModelRange) * yModelRange +" +
                " yModelRange,')')AS yModelName,count(1) AS num FROM (" + scoreSql + " ) a GROUP BY FLOOR(a.xModelName / xModelRange), " +
                "FLOOR(a.yModelName / yModelRange) ORDER BY FLOOR(a.xModelName / xModelRange), FLOOR(a.yModelName / yModelRange);";
        //替换变量
        scoreSql = scoreSql.replace("xModelName", fieldX).replace("xModelRange", fieldXRange).replace("yModelName", fieldY)
                .replace("yModelRange", fieldYRange);
        log.warn("多模型={} 统计sql={}", fieldX.concat(",").concat(fieldY), scoreSql);
        return reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);
    }

    /**
     * 画像模型任务统计计算
     *
     * @param fieldX
     * @param batchNumebrs
     * @param fieldXRange
     * @return List
     */
    private List<Map<String, Object>> imageModelCount(String fieldX, String batchNumebrs, String fieldXRange) {

        List<String> batchNumberList = Arrays.asList(batchNumebrs.split(","));

        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(" from b_score_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(" from b_score_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }
        }

        // 根据不同的画像模型使用不同的统计逻辑
        if ("pd_id_apply_age".equals(fieldX)) {
            // pd_id_apply_age使用10步长区间统计
            scoreSql = "SELECT " +
                    "concat('[',FLOOR(a.xModelName / xModelRange) * xModelRange,',',FLOOR(a.xModelName/xModelRange) * xModelRange + xModelRange,')')" +
                    "AS xModelName,count(1) AS num FROM (" + scoreSql + " ) a GROUP BY FLOOR(a.xModelName / xModelRange)" +
                    " ORDER BY FLOOR(a.xModelName / xModelRange);";
            //替换变量
            scoreSql = scoreSql.replace("xModelName", fieldX).replace("xModelRange", fieldXRange);
        } else if ("pd_id_gender".equals(fieldX) || "pd_cell_province".equals(fieldX) || "pd_cell_type".equals(fieldX)) {
            // pd_id_gender, pd_cell_province, pd_cell_type使用简单分组统计
            scoreSql = "SELECT " + fieldX + ", count(1) AS num FROM (" + scoreSql + " ) a GROUP BY " + fieldX + ";";
        }
        
        log.warn("画像模型={} 统计sql={}", fieldX, scoreSql);
        return reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);
    }

    /**
     * 单模型任务统计计算
     *
     * @param fieldX
     * @param batchNumebrs
     * @param fieldXRange
     * @return List
     */
    private List<Map<String, Object>> singleModelCount(String fieldX, String batchNumebrs, String fieldXRange) {

        List<String> batchNumberList = Arrays.asList(batchNumebrs.split(","));

        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(" from b_score_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(" from b_score_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }
        }
        //sql拼接【0,50)
        scoreSql = "SELECT " +
                "concat('[',FLOOR(a.xModelName / xModelRange) * xModelRange,',',FLOOR(a.xModelName/xModelRange) * xModelRange + xModelRange,')')" +
                "AS xModelName,count(1) AS num FROM (" + scoreSql + " ) a GROUP BY FLOOR(a.xModelName / xModelRange)" +
                " ORDER BY FLOOR(a.xModelName / xModelRange);";
        //替换变量
        scoreSql = scoreSql.replace("xModelName", fieldX).replace("xModelRange", fieldXRange);
        log.warn("单模型={} 统计sql={}", fieldX, scoreSql);
        return reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);
    }

    /**
     * 报表统计任务构建
     *
     * @param reportTask
     * @return
     */
    private void reportRuleBuild(ReportTask reportTask) {
        JSONObject reportRules = JSON.parseObject(reportTask.getReportRules());
        JSONObject batchNumerJson = reportRules.getJSONObject("productAndBatchNumber");
        List<ScoreReportRuleDTO> reportRuleList = reportRules.getJSONArray("rules").toJavaList(ScoreReportRuleDTO.class);
        reportRuleList.forEach((ScoreReportRuleDTO reportRule) -> {
            // 单模型
            if (CollectionUtils.isEmpty(reportRule.getY())) {
                List<String> xModelList = reportRule.getX();
                xModelList.forEach((String xModel) -> {
                    String batchNumberStr = batchNumerJson.getString(xModel);
                    Integer modelRange;
                    // 画像模型
                    if(IMAGEMODEL.contains(xModel)){
                        modelRange = 10;
                    }else {
                        modelRange = getModelRangeByDoris(xModel, batchNumberStr);
                        if(modelRange==null){
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), ("跑分模型统计异常,taskId=".
                                    concat(reportTask.getId().toString()).concat(" 单模型=").concat(xModel).concat("分值全为空"))));
                            return;
                        }
                    }
                    ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
                    statisticsScoreExample.createCriteria()
                            .andReportIdEqualTo(reportTask.getId())
                            .andFieldXRangeEqualTo(modelRange.toString())
                            .andReportScoreTypeEqualTo(1)
                            .andStatisticsOrderEqualTo(reportRule.getOrder())
                            .andIsDelEqualTo(Constants.DATA_VALID);
                    List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);
                    //同一规则：分位值相同更新，不同新增
                    if (CollectionUtils.isEmpty(statisticsScoreList)) {
                        ReportStatisticsScore statisticsScore = new ReportStatisticsScore();
                        statisticsScore.setReportId(reportTask.getId());
                        statisticsScore.setReportRule(JSON.toJSONString(reportRule));
                        statisticsScore.setReportScoreType(1);
                        JSONObject batchNumberJson = new JSONObject();
                        batchNumberJson.put(xModel, batchNumberStr);
                        statisticsScore.setBatchNumberList(batchNumberJson.toString());
                        statisticsScore.setFieldX(xModel);
                        statisticsScore.setFieldXRange(modelRange.toString());
                        statisticsScore.setStatisticsOrder(reportRule.getOrder());
                        statisticsScore.setIsDel(Constants.DATA_VALID);
                        statisticsScore.setCreateTime(new Date());
                        statisticsScore.setUpdateTime(new Date());
                        reportStatisticsScoreMapper.insertSelective(statisticsScore);
                    } else {
                        ReportStatisticsScore statisticsScore = statisticsScoreList.get(0);
                        ReportStatisticsScore update = new ReportStatisticsScore();
                        update.setId(statisticsScore.getId());
                        update.setFieldX(statisticsScore.getFieldX().concat(",").concat(xModel));
                        JSONObject batchNumberJson = JSONObject.parseObject(statisticsScore.getBatchNumberList());
                        batchNumberJson.put(xModel, batchNumberStr);
                        update.setBatchNumberList(batchNumberJson.toString());
                        reportStatisticsScoreMapper.updateByPrimaryKeySelective(update);
                    }

                });
            } else {
                //多模型
                List<String> xModelList = reportRule.getX();
                List<String> YModelList = reportRule.getY();
                //循环遍历X轴模型，Y轴模型
                xModelList.forEach((String xModel) -> {
                    YModelList.forEach((String yModel) -> {
                        List<String> xbatchNumber = new ArrayList<>(Arrays.asList(batchNumerJson.getString(xModel).split(",")));
                        List<String> ybatchNumber = new ArrayList<>(Arrays.asList(batchNumerJson.getString(yModel).split(",")));
                        //取交集 同时存在x，y模型
                        xbatchNumber.retainAll(ybatchNumber);
                        Integer  xModelRange = getModelRangeByDoris(xModel, batchNumerJson.getString(xModel));
                        Integer  yModelRange = getModelRangeByDoris(yModel, batchNumerJson.getString(yModel));
                        if (xModelRange == null || yModelRange == null) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), ("跑分模型统计异常,taskId=".concat
                                    (reportTask.getId().toString()).concat(" 多模型=").concat(xModel).concat("_").concat(yModel).concat("分值全为空"))));
                            return;
                        }
                        ReportStatisticsScore statisticsScore = new ReportStatisticsScore();
                        statisticsScore.setReportId(reportTask.getId());
                        statisticsScore.setReportRule(JSON.toJSONString(reportRule));
                        JSONObject batchNumberJson = new JSONObject();
                        batchNumberJson.put(xModel.concat("_").concat(yModel), String.join(",", xbatchNumber));
                        statisticsScore.setBatchNumberList(batchNumberJson.toString());
                        statisticsScore.setFieldX(xModel);
                        statisticsScore.setFieldY(yModel);
                        statisticsScore.setFieldXRange(xModelRange.toString());
                        statisticsScore.setFieldYRange(yModelRange.toString());
                        statisticsScore.setReportScoreType(2);
                        statisticsScore.setStatus(CollectionUtils.isEmpty(xbatchNumber) ? 3 : null);
                        statisticsScore.setStatisticsDesc(CollectionUtils.isEmpty(xbatchNumber) ? "模型不存在跑分文件" : null);
                        statisticsScore.setStatisticsOrder(reportRule.getOrder());
                        statisticsScore.setIsDel(Constants.DATA_VALID);
                        statisticsScore.setCreateTime(new Date());
                        statisticsScore.setUpdateTime(new Date());
                        reportStatisticsScoreMapper.insertSelective(statisticsScore);
                    });
                });
            }
        });

    }

    private Integer getModelRangeByDoris(String model, String batchNumberStr) {

        List<String> batchNumberList = Arrays.asList(batchNumberStr.split(","));

        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select max(").concat(model).concat(") as num from b_score_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select max(").concat(model).concat(") as num from b_score_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }
        }
        scoreSql = "select max(num) from ( ".concat(scoreSql).concat(") a;");

        Integer scoreValue = reportStatisticsScoreMapper.queryNumBybI_(scoreSql);
        //分值查询为空
        if (scoreValue == null) {
            return null;
        }
        Map<String, Integer> rangeConfig = marketingCommonConfig.getScoreReportRangeConfig();
        //speed配置
        return scoreValue > rangeConfig.get("scoreNum") ? rangeConfig.get("numRightStep") : rangeConfig.get("numLeftStep");

    }

    /**
     * 根据步长获取预定义的区间配置
     * 
     * @param stepLength 步长（5或50）
     * @return 预定义区间列表
     */
    private List<String> getPredefinedIntervals(Integer stepLength) {
        if (stepLength == null) {
            log.warn("步长为空，使用默认5步长区间");
            return marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");
        }
        if (stepLength.equals(5)) {
            return marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");
        } else if (stepLength.equals(50)) {
            return marketingCommonConfig.getBiReportStepConfig().get("fiftyStepLength");
        } else if (stepLength.equals(10)) {
            return marketingCommonConfig.getBiReportStepConfig().get("tenStepLength");
        } else {
            log.warn("未支持的步长: {}，使用默认5步长区间", stepLength);
            return marketingCommonConfig.getBiReportStepConfig().get("fiveStepLength");
        }
    }

    /**
     * 保存画像模型统计结果
     * 
     * @param statisticsId 统计ID
     * @param results 查询结果
     * @param model 模型名称
     */
    private void saveImageModelResults(Long statisticsId, List<Map<String, Object>> results, String model) {
        if ("pd_id_apply_age".equals(model)) {
            // pd_id_apply_age需要补充空区间，使用10步长的预定义区间
            List<String> predefinedIntervals = marketingCommonConfig.getBiReportStepConfig().get("tenStepLength");
            customIntervalStatistics.saveFixedIntervalResults(
                    statisticsId, results, model, model, predefinedIntervals);
        } else if ("pd_id_gender".equals(model) || "pd_cell_province".equals(model) || "pd_cell_type".equals(model)) {
            // pd_id_gender, pd_cell_province, pd_cell_type直接保存分组结果，不需要补充空区间
            List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
            if (!CollectionUtils.isEmpty(results)) {
                for (Map<String, Object> resultMap : results) {
                    ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
                    statisticsDetail.setStatisticsId(statisticsId);
                    statisticsDetail.setFieldXValue((String) resultMap.get(model));
                    statisticsDetail.setFieldYValue(model);
                    statisticsDetail.setFieldNum(((Long) resultMap.get("num")).intValue());
                    statisticsDetail.setCreateTime(new Date());
                    statisticsDetail.setUpdateTime(new Date());
                    statisticsDetails.add(statisticsDetail);
                }
            }
            
            if (!CollectionUtils.isEmpty(statisticsDetails)) {
                scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
            }
            
            log.info("保存画像模型结果完成，model: {}, statisticsId: {}, 结果数: {}", 
                    model, statisticsId, statisticsDetails.size());
        }
    }

    /**
     * 自定义区间报表统计
     *
     * @param reportTask 报表任务
     */
    private void customIntervalReportCount(ReportTask reportTask) {
        // 根据模板ID获取自定义区间配置
        ReportIntervalConfig reportIntervalConfig = reportIntervalConfigMapper.selectByPrimaryKey(Long.valueOf(reportTask.getTemplateId()));
        if (reportIntervalConfig == null) {
            log.warn("模板ID={} 未找到自定义区间配置", reportTask.getTemplateId());
            return;
        }

        // 获取模型配置列表
        ReportIntervalModelExample modelExample = new ReportIntervalModelExample();
        modelExample.createCriteria()
                .andConfigIdEqualTo(reportIntervalConfig.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        modelExample.setOrderByClause("`order` ASC");
        List<ReportIntervalModel> modelList = reportIntervalModelMapper.selectByExample(modelExample);
        
        if (CollectionUtils.isEmpty(modelList)) {
            log.warn("模板ID={} 未找到模型配置", reportTask.getTemplateId());
            return;
        }

        // 构建自定义区间统计任务
        customIntervalRuleBuild(reportTask, modelList);
        // 执行自定义区间统计
        customIntervalRuleCount(reportTask);
    }

    /**
     * 自定义区间规则构建
     *
     * @param reportTask 报表任务
     * @param modelList 模型配置列表
     */
    private void customIntervalRuleBuild(ReportTask reportTask, List<ReportIntervalModel> modelList) {
        JSONObject reportRules = JSON.parseObject(reportTask.getReportRules());
        JSONObject batchNumerJson = reportRules.getJSONObject("productAndBatchNumber");

        for (ReportIntervalModel model : modelList) {
            if ("1".equals(model.getAxisType())) {
                // 单模型
                buildSingleModelCustomInterval(reportTask, model, batchNumerJson);
            } else if ("2".equals(model.getAxisType())) {
                // 多模型
                buildMultiModelCustomInterval(reportTask, model, batchNumerJson);
            }
        }
    }

    /**
     * 构建单模型自定义区间
     */
    private void buildSingleModelCustomInterval(ReportTask reportTask, ReportIntervalModel model, JSONObject batchNumerJson) {
        if (StringUtils.isEmpty(model.getxModelName())) {
            return;
        }

        // 解析X模型名称列表
        List<String> xModelNames = new ArrayList<>(Arrays.asList(model.getxModelName().split(",")));
        if (CollectionUtils.isEmpty(xModelNames)) {
            return;
        }

        // 查找是否已存在相同区间配置的记录（类似原有逻辑：同一规则、分位值相同更新，不同新增）
        ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
        statisticsScoreExample.createCriteria()
                .andReportIdEqualTo(reportTask.getId())
                .andFieldXRangeEqualTo(model.getxIntervalList())
                .andReportScoreTypeEqualTo(1)
                .andStatisticsOrderEqualTo(Integer.valueOf(model.getOrder()))
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);

        JSONObject combinedBatchNumberJson = new JSONObject();
        StringBuilder fieldXBuilder = new StringBuilder();
        
        // 收集所有模型的批次号
        for (String xModel : xModelNames) {
            String batchNumberStr = batchNumerJson.getString(xModel);
            if (StringUtils.isEmpty(batchNumberStr)) {
                continue;
            }
            combinedBatchNumberJson.put(xModel, batchNumberStr);
            if (fieldXBuilder.length() > 0) {
                fieldXBuilder.append(",");
            }
            fieldXBuilder.append(xModel);
        }

        if (fieldXBuilder.length() == 0) {
            return;
        }

        // 同一规则：分位值相同更新，不同新增
        if (CollectionUtils.isEmpty(statisticsScoreList)) {
            ReportStatisticsScore statisticsScore = new ReportStatisticsScore();
            statisticsScore.setReportId(reportTask.getId());
            statisticsScore.setReportRule(JSON.toJSONString(model));
            statisticsScore.setReportScoreType(1);
            statisticsScore.setBatchNumberList(combinedBatchNumberJson.toString());
            statisticsScore.setFieldX(fieldXBuilder.toString());
            statisticsScore.setFieldXRange(model.getxIntervalList());
            statisticsScore.setStatisticsOrder(Integer.valueOf(model.getOrder()));
            statisticsScore.setIsDel(Constants.DATA_VALID);
            statisticsScore.setCreateTime(new Date());
            statisticsScore.setUpdateTime(new Date());
            reportStatisticsScoreMapper.insertSelective(statisticsScore);
        } else {
            // 更新现有记录
            ReportStatisticsScore existingScore = statisticsScoreList.get(0);
            ReportStatisticsScore updateScore = new ReportStatisticsScore();
            updateScore.setId(existingScore.getId());
            
            // 合并模型名称
            String existingFieldX = existingScore.getFieldX();
            updateScore.setFieldX(existingFieldX + "," + fieldXBuilder);
            
            // 合并批次号
            JSONObject existingBatchJson = JSONObject.parseObject(existingScore.getBatchNumberList());
            existingBatchJson.putAll(combinedBatchNumberJson);
            updateScore.setBatchNumberList(existingBatchJson.toString());
            updateScore.setUpdateTime(new Date());
            
            reportStatisticsScoreMapper.updateByPrimaryKeySelective(updateScore);
        }
    }

    /**
     * 构建多模型自定义区间
     */
    private void buildMultiModelCustomInterval(ReportTask reportTask, ReportIntervalModel model, JSONObject batchNumerJson) {
        if (StringUtils.isEmpty(model.getxModelName()) || StringUtils.isEmpty(model.getyModelName())) {
            return;
        }

        // 解析X、Y模型名称列表
        List<String> xModelNames = new ArrayList<>(Arrays.asList(model.getxModelName().split(",")));
        List<String> yModelNames = new ArrayList<>(Arrays.asList(model.getyModelName().split(",")));
        
        if (CollectionUtils.isEmpty(xModelNames) || CollectionUtils.isEmpty(yModelNames)) {
            return;
        }

        // 查找是否已存在相同区间配置的记录（同一规则：分位值相同更新，不同新增）
        ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
        statisticsScoreExample.createCriteria()
                .andReportIdEqualTo(reportTask.getId())
                .andFieldXRangeEqualTo(model.getxIntervalList())
                .andFieldYRangeEqualTo(model.getyIntervalList())
                .andReportScoreTypeEqualTo(2)
                .andStatisticsOrderEqualTo(Integer.valueOf(model.getOrder()))
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);

        JSONObject combinedBatchNumberJson = new JSONObject();
        StringBuilder fieldXBuilder = new StringBuilder();
        StringBuilder fieldYBuilder = new StringBuilder();
        
        // 收集所有模型组合的批次号
        for (String xModel : xModelNames) {
            for (String yModel : yModelNames) {
                String xBatchNumberStr = batchNumerJson.getString(xModel);
                String yBatchNumberStr = batchNumerJson.getString(yModel);
                
                if (StringUtils.isEmpty(xBatchNumberStr) || StringUtils.isEmpty(yBatchNumberStr)) {
                    continue;
                }

                // 取交集
                List<String> xBatchList = new ArrayList<>(Arrays.asList(xBatchNumberStr.split(",")));
                List<String> yBatchList = new ArrayList<>(Arrays.asList(yBatchNumberStr.split(",")));
                xBatchList.retainAll(yBatchList);

                if (CollectionUtils.isEmpty(xBatchList)) {
                    continue;
                }

                String modelPairKey = xModel.concat("_").concat(yModel);
                combinedBatchNumberJson.put(modelPairKey, String.join(",", xBatchList));
                
                if (fieldXBuilder.length() > 0) {
                    fieldXBuilder.append(",");
                    fieldYBuilder.append(",");
                }
                fieldXBuilder.append(xModel);
                fieldYBuilder.append(yModel);
            }
        }

        if (fieldXBuilder.length() == 0) {
            return;
        }

        // 同一规则：分位值相同更新，不同新增
        if (CollectionUtils.isEmpty(statisticsScoreList)) {
            ReportStatisticsScore statisticsScore = new ReportStatisticsScore();
            statisticsScore.setReportId(reportTask.getId());
            statisticsScore.setReportRule(JSON.toJSONString(model));
            statisticsScore.setReportScoreType(2);
            statisticsScore.setBatchNumberList(combinedBatchNumberJson.toString());
            statisticsScore.setFieldX(fieldXBuilder.toString());
            statisticsScore.setFieldY(fieldYBuilder.toString());
            statisticsScore.setFieldXRange(model.getxIntervalList());
            statisticsScore.setFieldYRange(model.getyIntervalList());
            statisticsScore.setStatisticsOrder(Integer.valueOf(model.getOrder()));
            statisticsScore.setIsDel(Constants.DATA_VALID);
            statisticsScore.setCreateTime(new Date());
            statisticsScore.setUpdateTime(new Date());
            reportStatisticsScoreMapper.insertSelective(statisticsScore);
        } else {
            // 更新现有记录
            ReportStatisticsScore existingScore = statisticsScoreList.get(0);
            ReportStatisticsScore updateScore = new ReportStatisticsScore();
            updateScore.setId(existingScore.getId());
            
            // 合并模型名称
            String existingFieldX = existingScore.getFieldX();
            String existingFieldY = existingScore.getFieldY();
            updateScore.setFieldX(existingFieldX + "," + fieldXBuilder.toString());
            updateScore.setFieldY(existingFieldY + "," + fieldYBuilder.toString());
            
            // 合并批次号
            JSONObject existingBatchJson = JSONObject.parseObject(existingScore.getBatchNumberList());
            existingBatchJson.putAll(combinedBatchNumberJson);
            updateScore.setBatchNumberList(existingBatchJson.toString());
            updateScore.setUpdateTime(new Date());
            
            reportStatisticsScoreMapper.updateByPrimaryKeySelective(updateScore);
        }
    }

    /**
     * 自定义区间统计计算
     */
    private void customIntervalRuleCount(ReportTask reportTask) {
        ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
        statisticsScoreExample.createCriteria()
                .andReportIdEqualTo(reportTask.getId())
                .andStatusIsNull()
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ReportStatisticsScore> statisticsScoreList = reportStatisticsScoreMapper.selectByExample(statisticsScoreExample);
        
        if (CollectionUtils.isEmpty(statisticsScoreList)) {
            return;
        }

        statisticsScoreList.forEach(statisticsScore -> {
            try {
                if (statisticsScore.getReportScoreType().equals(1)) {
                    // 单模型自定义区间统计
                    customSingleModelCount(statisticsScore);
                } else {
                    // 多模型自定义区间统计
                    customMultiModelCount(statisticsScore);
                }
                updateReportScore(statisticsScore, 1, null);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "自定义区间统计异常"), e);
                updateReportScore(statisticsScore, 2, "自定义区间统计异常");
            }
        });
    }

    /**
     * 单模型自定义区间统计
     */
    private void customSingleModelCount(ReportStatisticsScore statisticsScore) {
        List<IntervalRangeDTO> xIntervalList = JSON.parseArray(statisticsScore.getFieldXRange(), IntervalRangeDTO.class);
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
     * 多模型自定义区间统计
     */
    private void customMultiModelCount(ReportStatisticsScore statisticsScore) {
        List<IntervalRangeDTO> xIntervalList = JSON.parseArray(statisticsScore.getFieldXRange(), IntervalRangeDTO.class);
        List<IntervalRangeDTO> yIntervalList = JSON.parseArray(statisticsScore.getFieldYRange(), IntervalRangeDTO.class);
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

}