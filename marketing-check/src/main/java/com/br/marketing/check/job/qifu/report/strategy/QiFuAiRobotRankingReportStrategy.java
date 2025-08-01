package com.br.marketing.check.job.qifu.report.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.BillReport;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.entity.QiFuAiRobotRankingReportData;
import com.br.marketing.entity.excel.QiFuAiRobotRankingReportExcelModel;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuAiRobotRankingReportStrategy
 * @Author hang.zhou
 * @Date 2025/7/29
 */
@Component("qiFuAiRobotRankingReportStrategy")
public class QiFuAiRobotRankingReportStrategy implements ReportStrategy<QiFuAiRobotRankingReportData, QiFuAiRobotRankingReportExcelModel> {

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public String getApiCode(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        return StringUtils.isNotEmpty(jobParameter) ? jobParameter : marketingCommonConfig.getQiFuAIUploadDataApiCode();
    }

    @Override
    public Integer getSubjectCode() {
        return 3;
    }

    @Override
    public List<QiFuAiRobotRankingReportData> queryData(String apiCode, String currentDate) {
        List<QiFuAiRobotRankingReportData> qiFuAiRobotRankingReportDataList = new ArrayList<>();
        List<DrsCustomizeUploadData> drsCustomizeUploadDataList = drsCustomizeUploadDataMapper.selectByApiCodeAndDate("_robot_ranking_report", apiCode, currentDate);
        if (!drsCustomizeUploadDataList.isEmpty()) {
            DrsCustomizeUploadData drsCustomizeUploadData = drsCustomizeUploadDataList.get(0);
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(drsCustomizeUploadData));
            String requestJsonData = jsonObject.getString("requestJsonData");
            QiFuAiRobotRankingReportData qiFuAiRobotRankingReportData = JSONObject.parseObject(requestJsonData, QiFuAiRobotRankingReportData.class);
            qiFuAiRobotRankingReportDataList.add(qiFuAiRobotRankingReportData);
            return qiFuAiRobotRankingReportDataList;
        }
        return Collections.emptyList();
    }

    @Override
    public List<QiFuAiRobotRankingReportExcelModel> convertToExcelModel(List<QiFuAiRobotRankingReportData> dataList) {
        List<BillReport> billReportList = dataList.get(0).getBillReportList();
        return billReportList.stream()
                .map(billReport -> {
                    QiFuAiRobotRankingReportExcelModel reportExcelModel = new QiFuAiRobotRankingReportExcelModel();
                    BeanUtils.copyProperties(billReport, reportExcelModel);
                    return reportExcelModel;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<QiFuAiRobotRankingReportExcelModel> postProcess(List<QiFuAiRobotRankingReportExcelModel> excelList) {
        return excelList;
    }

    @Override
    public Class<QiFuAiRobotRankingReportExcelModel> getExcelModelClass() {
        return QiFuAiRobotRankingReportExcelModel.class;
    }

    @Override
    public String getSheetName() {
        return "360AI语音机器人排名";
    }

    @Override
    public String getContent(String subject) {
        return "360AI语音机器人排名报表";
    }

    @Override
    public String getAttachmentFileName(String subject) {
        String currentDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        return subject.concat("_").concat(currentDate);
    }

}