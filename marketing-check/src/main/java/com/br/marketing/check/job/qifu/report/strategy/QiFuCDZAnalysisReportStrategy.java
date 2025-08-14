package com.br.marketing.check.job.qifu.report.strategy;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QifuActuation;
import com.br.marketing.entity.QifuActuationExample;
import com.br.marketing.entity.excel.QiFuCuDongAnalysisReportExcelModel;
import com.br.marketing.mapper.QifuActuationMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 360促动支分析报表策略
 * 在postProcess方法中实现单元格合并逻辑
 */
@Component("qiFuCDZAnalysisReportStrategy")
public class QiFuCDZAnalysisReportStrategy implements ReportStrategy<QifuActuation, QiFuCuDongAnalysisReportExcelModel> {

    @Resource
    private QifuActuationMapper qifuActuationMapper;

    @Override
    public String getApiCode(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        return StringUtils.isNotEmpty(jobParameter) ? jobParameter : "3710139";
    }

    @Override
    public Integer getSubjectCode() {
        return 2;
    }

    @Override
    public List<QifuActuation> queryData(String apiCode, String currentDate) {
        QifuActuationExample example = new QifuActuationExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andCreateDateEqualTo(currentDate)
                .andIsDelEqualTo(1);
        return qifuActuationMapper.selectByExample(example);
    }

    @Override
    public List<QiFuCuDongAnalysisReportExcelModel> convertToExcelModel(List<QifuActuation> dataList) {
        return dataList.stream()
                .map((QifuActuation qifuActuation) -> {
                    qifuActuation.setAppLoginRate(convertPercent(qifuActuation.getAppLoginRate(), 2));
                    qifuActuation.setUserLoanRate(convertPercent(qifuActuation.getUserLoanRate(), 5));
                    qifuActuation.setUserStartRate(convertPercent(qifuActuation.getUserStartRate(), 2));
                    QiFuCuDongAnalysisReportExcelModel reportExcelModel = new QiFuCuDongAnalysisReportExcelModel();
                    BeanUtils.copyProperties(qifuActuation, reportExcelModel);
                    return reportExcelModel;
                }).collect(Collectors.toList());
    }

    @Override
    public List<QiFuCuDongAnalysisReportExcelModel> postProcess(List<QiFuCuDongAnalysisReportExcelModel> excelList) {
        //按照下发日期 > usertype > 供应商对数据排序
        excelList.sort(Comparator
                .comparing(QiFuCuDongAnalysisReportExcelModel::getIssueDate, Comparator.nullsFirst(String::compareTo))
                .thenComparing(QiFuCuDongAnalysisReportExcelModel::getUserType, Comparator.nullsFirst(String::compareTo))
                .thenComparing(QiFuCuDongAnalysisReportExcelModel::getSupplier, Comparator.nullsFirst(String::compareTo)));
        return excelList;
    }

    @Override
    public Class<QiFuCuDongAnalysisReportExcelModel> getExcelModelClass() {
        return QiFuCuDongAnalysisReportExcelModel.class;
    }

    @Override
    public String getSheetName() {
        return "360促动支分析效果统计";
    }

    @Override
    public String getContent(String subject) {
        return subject.concat(": ").concat("促动支分析效果数据报表");
    }

    @Override
    public String getAttachmentFileName(String subject) {
        String currentDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        return subject.concat("_").concat(currentDate);
    }

    /**
     * 小数转百分比
     *
     * @param value 原值
     * @param num   保留小数点后位数
     */
    public static String convertPercent(String value, Integer num) {
        if (value == null) return null;
        double percent = Double.parseDouble(value) * 100;
        String format = "%." + num + "f%%";
        return String.format(format, percent);
    }
}
