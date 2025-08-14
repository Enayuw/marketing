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
import java.time.format.DateTimeFormatter;
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
                .map((QiFuEffectReportData qiFuEffectReportData) -> {
                    //百分比取8位小数
                    qiFuEffectReportData.setLoginRate(convertPercent(qiFuEffectReportData.getLoginRate(), 8));
                    qiFuEffectReportData.setApplySubmitRate(convertPercent(qiFuEffectReportData.getApplySubmitRate(), 8));
                    qiFuEffectReportData.setPassRate(convertPercent(qiFuEffectReportData.getPassRate(), 8));
                    qiFuEffectReportData.setCreditSuccessRate(convertPercent(qiFuEffectReportData.getCreditSuccessRate(), 8));
                    qiFuEffectReportData.setDeltaApplySubmitRate(convertPercent(qiFuEffectReportData.getDeltaApplySubmitRate(), 8));
                    qiFuEffectReportData.setDeltaCreditSuccessRate(convertPercent(qiFuEffectReportData.getDeltaCreditSuccessRate(), 8));
                    qiFuEffectReportData.setAttrApplyRatio(convertPercent(qiFuEffectReportData.getAttrApplyRatio(), 8));
                    qiFuEffectReportData.setAttrCreditRatio(convertPercent(qiFuEffectReportData.getAttrCreditRatio(), 8));
                    qiFuEffectReportData.setAttrApplyRate(convertPercent(qiFuEffectReportData.getAttrApplyRate(), 8));
                    qiFuEffectReportData.setAttrCreditRate(convertPercent(qiFuEffectReportData.getAttrCreditRate(), 8));
                    //数量四舍五入取整
                    qiFuEffectReportData.setUserCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getUserCount()))));
                    qiFuEffectReportData.setLoginUserCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getLoginUserCount()))));
                    qiFuEffectReportData.setApplySubmitUserCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getApplySubmitUserCount()))));
                    qiFuEffectReportData.setCreditSuccessUserCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getCreditSuccessUserCount()))));
                    qiFuEffectReportData.setDeltaApplySubmitCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getDeltaApplySubmitCount()))));
                    qiFuEffectReportData.setDeltaCreditSuccessCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getDeltaCreditSuccessCount()))));
                    qiFuEffectReportData.setAttrApplyUserCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getAttrApplyUserCount()))));
                    qiFuEffectReportData.setAttrCreditUserCount(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getAttrCreditUserCount()))));
                    qiFuEffectReportData.setAttrCreditUserCountA(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getAttrCreditUserCountA()))));
                    qiFuEffectReportData.setAttrCreditUserCountB(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getAttrCreditUserCountB()))));
                    qiFuEffectReportData.setAttrCreditUserCountC(String.valueOf(Math.round(Float.parseFloat(qiFuEffectReportData.getAttrCreditUserCountC()))));

                    QiFuEffectReportExcelModel reportExcelModel = new QiFuEffectReportExcelModel();
                    BeanUtils.copyProperties(qiFuEffectReportData, reportExcelModel);
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
        String currentDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        return subject.concat("_").concat(currentDate);
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
