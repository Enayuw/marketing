package com.br.marketing.check.service.Impl.scorereport;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.report.ScoreReportRuleDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ReportStatisticsScoreBaseMapper;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.mapper.ScoreStatisticsDetailBaseMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.vo.VariableDicSelectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class ScoreReportTaskServiceImpl implements ScoreReportTaskService {


    @Resource
    private XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Resource
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Resource
    private ScoreStatisticsDetailBaseMapper scoreStatisticsDetailBaseMapper;


    public static String X_MODEL = "xModel";
    public static String X_MODEL_RANGE = "xModelRange";
    public static String Y_MODEL = "yModel";
    public static String Y_MODEL_RANGE = "yModelRange";


    @Override
    public void scoreReportCount(ReportTask reportTask) {
        String reportRules = reportTask.getReportRules();
        //解析规则，生成报表
        reportRuleBuild(reportTask);
        //跑分统计计算
        reportRuleCount(reportTask);

    }

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
                        String batchNumebrs = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(model);
                        List<Map<String, String>> singleResult = singleModelCount(model, batchNumebrs, statisticsScore.getFieldXRange());
                        singleResult.forEach((Map<String, String> resultMap) -> {
                            ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
                            statisticsDetail.setStatisticsId(statisticsScore.getId());
                            statisticsDetail.setFieldXValue(resultMap.get(model));
                            statisticsDetail.setFieldNum(Integer.valueOf(resultMap.get("num")));
                            statisticsDetail.setCreateTime(new Date());
                            statisticsDetail.setUpdateTime(new Date());
                            scoreStatisticsDetailBaseMapper.insertSelective(statisticsDetail);
                        });

                    });
                    updateReportScore(statisticsScore, 1, null);
                } catch (Exception e) {
                    log.error("跑分模型统计异常", e);
                    updateReportScore(statisticsScore, 2, null);
                }
            } else {
                //多模型统计
                String batchNumebrs = JSONObject.parseObject(statisticsScore.getBatchNumberList()).getString(statisticsScore.getFieldX().concat("_").concat(statisticsScore.getFieldY()));
                List<Map<String, String>> mulResult = mulModelCount(statisticsScore.getFieldX(), statisticsScore.getFieldY(),
                        batchNumebrs, statisticsScore.getFieldXRange(), statisticsScore.getFieldYRange());
                try {
                    mulResult.forEach((Map<String, String> resultMap) -> {
                        ScoreStatisticsDetail statisticsDetail = new ScoreStatisticsDetail();
                        statisticsDetail.setStatisticsId(statisticsScore.getId());
                        statisticsDetail.setFieldXValue(resultMap.get(statisticsScore.getFieldX()));
                        statisticsDetail.setFieldYValue(resultMap.get(statisticsScore.getFieldY()));
                        statisticsDetail.setFieldNum(Integer.valueOf(resultMap.get("num")));
                        statisticsDetail.setCreateTime(new Date());
                        statisticsDetail.setUpdateTime(new Date());
                        scoreStatisticsDetailBaseMapper.insertSelective(statisticsDetail);
                    });
                    updateReportScore(statisticsScore, 1, null);
                } catch (Exception e) {
                    log.error("跑分模型统计异常", e);
                    updateReportScore(statisticsScore, 2, null);

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

    private List<Map<String, String>> mulModelCount(String fieldX, String fieldY, String batchNumebrs, String fieldXRange, String fieldYRange) {

        List<String> batchNumberList = Arrays.asList(batchNumebrs.split(","));

        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(",").concat(fieldY).concat(" from b_socre_report_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select ").concat(fieldX).concat(",").concat(fieldY).concat(" from b_socre_report_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }

        }
        scoreSql = "SELECT concat( '[', FLOOR(a.xModel / xModelRange) * xModelRange, '-', FLOOR(a.xModel / xModelRange) * xModelRange + xModelRange, ']') AS xModel,"
                + "concat( '[', FLOOR(a.yModel / yModelRange) * yModelRange, '-', FLOOR(a.yModel / yModelRange) * yModelRange + yModelRange, ']' ) AS yModel, " +
                "count(1) AS ’num‘ FROM (" + scoreSql + " ) a GROUP BY FLOOR(a.xModel / xModelRange), FLOOR(a.yModel / yModelRange)" +
                " ORDER BY FLOOR(a.xModel / xModelRange), FLOOR(a.yModel / yModelRange)";
        scoreSql.replace("xModel", fieldX).replace("xModelRange", fieldXRange).replace("yModel", fieldY).replace("yModelRange", fieldYRange);
        return reportStatisticsScoreMapper.queryDataMapNumdoris_(scoreSql);
    }


    private List<Map<String, String>> singleModelCount(String model, String batchNumebrs, String fieldXRange) {

        List<String> batchNumberList = Arrays.asList(batchNumebrs.split(","));

        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select ").concat(model).concat(" from b_socre_report_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select ").concat(model).concat(" from b_socre_report_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }

        }

        scoreSql = "select concat('[',FLOOR(a.".concat(model).concat("/").concat(fieldXRange).concat(") * ").concat(fieldXRange).concat(",'-',FLOOR(a.")
                .concat(model).concat("/").concat(fieldXRange).concat(") * ").concat(fieldXRange).concat(" +5,']') as ").concat(model)
                .concat(",count(1) AS num from").concat(scoreSql).concat(" a GROUP BY FLOOR(a.").concat(model).concat("/").concat(fieldXRange)
                .concat(")ORDER BY FLOOR(a.").concat(model).concat("/").concat(fieldXRange).concat(",");

        return reportStatisticsScoreMapper.queryDataMapNumdoris_(scoreSql);


    }


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
                xModelList.forEach(xModel -> {
                    YModelList.forEach(yModel -> {
                        List xbatchNumber = new ArrayList(Arrays.asList(batchNumerJson.getString(xModel)));
                        List ybatchNumber = new ArrayList(Arrays.asList(batchNumerJson.getString(yModel)));
                        //取交集
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
                scoreSql = scoreSql.concat("select max(").concat(model).concat(") as num from b_socre_report_").concat(batchNumberList.get(i));
            } else {
                scoreSql = scoreSql.concat("select max(").concat(model).concat(") as num from b_socre_report_").concat(batchNumberList.get(i))
                        .concat(" union all ");
            }

        }
        scoreSql = "select max(num) from ( ".concat(scoreSql).concat(") a;");

        Integer scoreValue = scoreRecordMapper.getXieChengDataNumdoris_(scoreSql);
        if (scoreValue > 100) {
            return 50;
        } else {
            return 5;
        }
    }


    private Integer queryDoris(String sql) {

        Integer total = null;
        // 查询Doris
        try {
            total = scoreRecordMapper.getXieChengDataNumdoris_(sql);
        } catch (Exception e) {
            log.error("筛选查询Doris异常,sql={}", sql, e);
        }
        return total;
    }
}