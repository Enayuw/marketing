package com.br.marketing.check.service.Impl.scorereport;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.report.ScoreReportRuleDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.report.ReportTaskStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.bi.AnalysisReportService;
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
    private XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Resource
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Resource
    private ScoreStatisticsDetailMapper scoreStatisticsDetailMapper;

    @Resource
    private ReportTaskMapper reportTaskMapper;


    @Resource
    private AnalysisReportService analysisReportService;


    @Override
    public void scoreReportCount(ReportTask reportTask) {
        //解析规则，生成报表
        reportRuleBuild(reportTask);
        //跑分统计计算
        reportRuleCount(reportTask);
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
        reportTask.setStatus(failNum > 0 ? ReportTaskStatusEnum.FAIL.getValue() : ReportTaskStatusEnum.SUCCESS.getValue());
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
                List<String> modelList = new ArrayList(Arrays.asList(statisticsScore.getFieldX().split(",")));
                try {
                    modelList.forEach(model -> {
                        List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
                        String batchNumebrs = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(model);
                        List<Map<String, Object>> singleResult = singleModelCount(model, batchNumebrs, statisticsScore.getFieldXRange());
                        singleResult.forEach((Map<String, Object> resultMap) -> {
                            ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
                            statisticsDetail.setStatisticsId(statisticsScore.getId());
                            statisticsDetail.setFieldXValue(StringUtils.isEmpty(resultMap.get(model)) ? "[-1,0)" : (String) resultMap.get(model));
                            //单模型Y存储模型名称
                            statisticsDetail.setFieldYValue(model);
                            statisticsDetail.setFieldNum(((Long) resultMap.get("num")).intValue());
                            statisticsDetail.setCreateTime(new Date());
                            statisticsDetail.setUpdateTime(new Date());
                            statisticsDetails.add(statisticsDetail);
                        });
                        //批量插入结果
                        scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
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
                    List<ScoreStatisticsDetail> statisticsDetails = new ArrayList<>();
                    mulResult.forEach((Map<String, Object> resultMap) -> {
                        ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
                        statisticsDetail.setStatisticsId(statisticsScore.getId());
                        statisticsDetail.setFieldXValue(StringUtils.isEmpty(resultMap.get(statisticsScore.getFieldX())) ? "[-1,0)" :
                                (String) resultMap.get(statisticsScore.getFieldX()));
                        statisticsDetail.setFieldYValue(StringUtils.isEmpty(resultMap.get(statisticsScore.getFieldY())) ? "[-1,0)" :
                                (String) resultMap.get(statisticsScore.getFieldY()));
                        statisticsDetail.setFieldNum(((Long) resultMap.get("num")).intValue());
                        statisticsDetail.setCreateTime(new Date());
                        statisticsDetail.setUpdateTime(new Date());
                        statisticsDetails.add(statisticsDetail);
                    });
                    scoreStatisticsDetailMapper.insertBatch(statisticsDetails);
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
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(",").concat(fieldY).concat(" from b_score_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(",").concat(fieldY).concat(" from b_score_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }

        }
        scoreSql = "SELECT " +
                "concat('[',FLOOR(a.xModelName/xModelRange) * xModelRange,',',FLOOR(a.xModelName/xModelRange) * xModelRange + xModelRange,')')AS xModelName," +
                "concat('[',FLOOR(a.yModelName/yModelRange) * yModelRange,',',FLOOR(a.yModelName/yModelRange) * yModelRange + yModelRange,')')AS yModelName," +
                "count(1) AS num FROM (" + scoreSql + " ) a GROUP BY FLOOR(a.xModelName / xModelRange), FLOOR(a.yModelName / yModelRange)" +
                " ORDER BY FLOOR(a.xModelName / xModelRange), FLOOR(a.yModelName / yModelRange);";
        //替换变量
        scoreSql = scoreSql.replace("xModelName", fieldX).replace("xModelRange", fieldXRange).replace("yModelName", fieldY)
                .replace("yModelRange", fieldYRange);
        return reportStatisticsScoreMapper.queryDataMapNumdoris_(scoreSql);
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
                "concat('[',FLOOR(a.xModelName / xModelRange) * xModelRange,',',FLOOR(a.xModelName/xModelRange) * xModelRange + xModelRange,')')AS xModelName" +
                ",count(1) AS num FROM (" + scoreSql + " ) a GROUP BY FLOOR(a.xModelName / xModelRange)" +
                " ORDER BY FLOOR(a.xModelName / xModelRange);";
        //替换变量
        scoreSql = scoreSql.replace("xModelName", fieldX).replace("xModelRange", fieldXRange);
        return reportStatisticsScoreMapper.queryDataMapNumdoris_(scoreSql);
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
        reportRuleList.forEach(reportRule -> {
            // 单模型
            if (CollectionUtils.isEmpty(reportRule.getY())) {
                List<String> xModelList = reportRule.getX();
                xModelList.forEach(xModel -> {
                    String batchNumberStr = batchNumerJson.getString(xModel);
                    String modelRange = getModelRangeByDoris(xModel, batchNumberStr).toString();
                    ReportStatisticsScoreExample statisticsScoreExample = new ReportStatisticsScoreExample();
                    statisticsScoreExample.createCriteria()
                            .andReportIdEqualTo(reportTask.getId())
                            .andFieldXRangeEqualTo(modelRange)
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
                        statisticsScore.setFieldXRange(modelRange);
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
                xModelList.forEach(xModel -> {
                    YModelList.forEach(yModel -> {
                        List xbatchNumber = new ArrayList(Arrays.asList(batchNumerJson.getString(xModel).split(",")));
                        List ybatchNumber = new ArrayList(Arrays.asList(batchNumerJson.getString(yModel).split(",")));
                        //取交集 同时存在x，y模型
                        xbatchNumber.retainAll(ybatchNumber);
                        ReportStatisticsScore statisticsScore = new ReportStatisticsScore();
                        statisticsScore.setReportId(reportTask.getId());
                        statisticsScore.setReportRule(JSON.toJSONString(reportRule));
                        JSONObject batchNumberJson = new JSONObject();
                        batchNumberJson.put(xModel.concat("_").concat(yModel), String.join(",", xbatchNumber));
                        statisticsScore.setBatchNumberList(batchNumberJson.toString());
                        statisticsScore.setFieldX(xModel);
                        statisticsScore.setFieldY(yModel);
                        statisticsScore.setFieldXRange(getModelRangeByDoris(xModel, batchNumerJson.getString(xModel)).toString());
                        statisticsScore.setFieldYRange(getModelRangeByDoris(yModel, batchNumerJson.getString(yModel)).toString());
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

        Integer scoreValue = scoreRecordMapper.getXieChengDataNumdoris_(scoreSql);
        //考虑speed配置
        return scoreValue > 200 ? 50 : 5;

    }

}