package com.br.marketing.check.job.qifu.report.strategy;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QiFuEffectReportData;
import com.br.marketing.entity.QiFuEffectReportDataExample;
import com.br.marketing.entity.excel.QiFuEffectReportExcelModel;
import com.br.marketing.mapper.QiFuEffectReportDataMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuEffectReportStrategy
 * @Author hang.zhou
 * @Date 2025/8/13
 */
@Component("qiFuEffectReportStrategy")
public class QiFuEffectReportStrategy implements ReportStrategy<QiFuEffectReportData, QiFuEffectReportExcelModel> {

    private static final Logger logger = LoggerFactory.getLogger(QiFuEffectReportStrategy.class);

    @Resource
    private QiFuEffectReportDataMapper qiFuEffectReportDataMapper;

    @Override
    public String getApiCode(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        return StringUtils.isNotEmpty(jobParameter) ? jobParameter : "3710053";
    }

    @Override
    public Integer getSubjectCode() {
        return 4;
    }

    @Override
    public List<QiFuEffectReportData> queryData(String apiCode, String currentDate) {
        QiFuEffectReportDataExample example = new QiFuEffectReportDataExample();
        LocalDate localDate = LocalDate.parse(currentDate);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andCreateTimeGreaterThan(date)
                .andIsDelEqualTo(1);
        return qiFuEffectReportDataMapper.selectByExample(example);
    }

    @Override
    public List<QiFuEffectReportExcelModel> convertToExcelModel(List<QiFuEffectReportData> dataList) {
        return dataList.stream()
                .map((QiFuEffectReportData QiFuEffectReportData) -> {
                    QiFuEffectReportData.setLoginRate(convertPercent(QiFuEffectReportData.getLoginRate(), 6));
                    QiFuEffectReportData.setApplySubmitRate(convertPercent(QiFuEffectReportData.getApplySubmitRate(), 6));
                    QiFuEffectReportData.setPassRate(convertPercent(QiFuEffectReportData.getPassRate(), 6));
                    QiFuEffectReportData.setCreditSuccessRate(convertPercent(QiFuEffectReportData.getCreditSuccessRate(), 6));
                    QiFuEffectReportData.setDeltaApplySubmitRate(convertPercent(QiFuEffectReportData.getDeltaApplySubmitRate(), 6));
                    QiFuEffectReportData.setDeltaCreditSuccessRate(convertPercent(QiFuEffectReportData.getDeltaCreditSuccessRate(), 6));
                    QiFuEffectReportData.setAttrApplyRatio(convertPercent(QiFuEffectReportData.getAttrApplyRatio(), 6));
                    QiFuEffectReportData.setAttrCreditRatio(convertPercent(QiFuEffectReportData.getAttrCreditRatio(), 6));
                    QiFuEffectReportData.setAttrApplyRate(convertPercent(QiFuEffectReportData.getAttrApplyRate(), 6));
                    QiFuEffectReportData.setAttrCreditRate(convertPercent(QiFuEffectReportData.getAttrCreditRate(), 6));
                    QiFuEffectReportExcelModel reportExcelModel = new QiFuEffectReportExcelModel();
                    BeanUtils.copyProperties(QiFuEffectReportData, reportExcelModel);
                    return reportExcelModel;
                }).collect(Collectors.toList());
    }

    @Override
    public List<QiFuEffectReportExcelModel> postProcess(List<QiFuEffectReportExcelModel> excelList) {
        return excelList;
    }

    @Override
    public Class<QiFuEffectReportExcelModel> getExcelModelClass() {
        return QiFuEffectReportExcelModel.class;
    }

    @Override
    public String getSheetName() {
        return "360促申效果统计数据";
    }

    @Override
    public String getContent(String subject) {
        return "360促申效果统计数据报表";
    }

    @Override
    public String getAttachmentFileName(String subject) {
        return subject;
    }

    /**
     * 小数转百分比
     *
     * @param value     原值
     * @param precision 保留小数点后位数
     * @return 百分比字符串，如果输入为null则返回null
     */
    public static String convertPercent(String value, Integer precision) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            double percent = Double.parseDouble(value) * 100;
            String format = "%." + precision + "f%%";
            return String.format(format, percent);
        } catch (NumberFormatException e) {
            logger.warn("百分比转换失败，原值: {}, 精度: {}", value, precision, e);
            return value; // 转换失败时返回原值
        }
    }
}
