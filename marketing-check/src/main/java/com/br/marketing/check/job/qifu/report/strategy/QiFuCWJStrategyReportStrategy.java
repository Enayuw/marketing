package com.br.marketing.check.job.qifu.report.strategy;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.entity.QifuStrategyReportDataExample;
import com.br.marketing.entity.excel.QiFuStrategyReportExcelModel;
import com.br.marketing.mapper.QifuStrategyReportDataMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuCWJStrategyReportStrategy
 * @Author hang.zhou
 * @Date 2025/7/18
 */
@Component("qiFuCWJStrategyReportStrategy")
public class QiFuCWJStrategyReportStrategy implements ReportStrategy<QifuStrategyReportData, QiFuStrategyReportExcelModel>{

    @Resource
    private QifuStrategyReportDataMapper qifuStrategyReportDataMapper;

    @Override
    public String getApiCode(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        return StringUtils.isNotEmpty(jobParameter) ? jobParameter : "3710053";
    }

    @Override
    public Integer getSubjectCode() {
        return 1;
    }

    @Override
    public List<QifuStrategyReportData> queryData(String apiCode, String currentDate) {
        QifuStrategyReportDataExample example = new QifuStrategyReportDataExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andCreateTimeGreaterThan(java.sql.Date.valueOf(currentDate));
        return qifuStrategyReportDataMapper.selectByExample(example);
    }

    @Override
    public List<QiFuStrategyReportExcelModel> convertToExcelModel(List<QifuStrategyReportData> dataList) {
        return dataList.stream().map(reportData -> {
            QiFuStrategyReportExcelModel model = new QiFuStrategyReportExcelModel();
            BeanUtils.copyProperties(reportData, model);
            model.setStrategyDate(LocalDate.now().toString());
            return model;
        }).collect(Collectors.toList());
    }

    @Override
    public List<QiFuStrategyReportExcelModel> postProcess(List<QiFuStrategyReportExcelModel> excelList) {
        return excelList;
    }

    @Override
    public Class<QiFuStrategyReportExcelModel> getExcelModelClass() {
        return QiFuStrategyReportExcelModel.class;
    }

    @Override
    public String getSheetName() {
        return "360策略效果统计";
    }

    @Override
    public String getContent(String subject) {
        return subject.concat(": ").concat("策略效果数据报表");
    }

    @Override
    public String getAttachmentFileName(String subject) {
        String currentDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        return subject.concat("_").concat(currentDate);
    }

}
