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
                .map(this::convertSingleRecord)
                .collect(Collectors.toList());
    }

    /**
     * 转换单条记录
     */
    private QiFuEffectReportExcelModel convertSingleRecord(QiFuEffectReportData data) {
        // 处理百分比字段（保留8位小数）
        processPercentFields(data);

        // 处理数量字段（四舍五入取整）
        processCountFields(data);

        // 转换为Excel模型
        QiFuEffectReportExcelModel excelModel = new QiFuEffectReportExcelModel();
        BeanUtils.copyProperties(data, excelModel);
        return excelModel;
    }

    /**
     * 处理百分比字段
     */
    private void processPercentFields(QiFuEffectReportData data) {
        data.setLoginRate(convertPercent(data.getLoginRate(), 8));
        data.setApplySubmitRate(convertPercent(data.getApplySubmitRate(), 8));
        data.setPassRate(convertPercent(data.getPassRate(), 8));
        data.setCreditSuccessRate(convertPercent(data.getCreditSuccessRate(), 8));
        data.setDeltaApplySubmitRate(convertPercent(data.getDeltaApplySubmitRate(), 8));
        data.setDeltaCreditSuccessRate(convertPercent(data.getDeltaCreditSuccessRate(), 8));
        data.setAttrApplyRatio(convertPercent(data.getAttrApplyRatio(), 8));
        data.setAttrCreditRatio(convertPercent(data.getAttrCreditRatio(), 8));
        data.setAttrApplyRate(convertPercent(data.getAttrApplyRate(), 8));
        data.setAttrCreditRate(convertPercent(data.getAttrCreditRate(), 8));
    }

    /**
     * 处理数量字段
     */
    private void processCountFields(QiFuEffectReportData data) {
        data.setUserCount(roundToInteger(data.getUserCount()));
        data.setLoginUserCount(roundToInteger(data.getLoginUserCount()));
        data.setApplySubmitUserCount(roundToInteger(data.getApplySubmitUserCount()));
        data.setCreditSuccessUserCount(roundToInteger(data.getCreditSuccessUserCount()));
        data.setDeltaApplySubmitCount(roundToInteger(data.getDeltaApplySubmitCount()));
        data.setDeltaCreditSuccessCount(roundToInteger(data.getDeltaCreditSuccessCount()));
        data.setAttrApplyUserCount(roundToInteger(data.getAttrApplyUserCount()));
        data.setAttrCreditUserCount(roundToInteger(data.getAttrCreditUserCount()));
        data.setAttrCreditUserCountA(roundToInteger(data.getAttrCreditUserCountA()));
        data.setAttrCreditUserCountB(roundToInteger(data.getAttrCreditUserCountB()));
        data.setAttrCreditUserCountC(roundToInteger(data.getAttrCreditUserCountC()));
    }

    /**
     * 四舍五入取整
     */
    private String roundToInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        try {
            return String.valueOf(Math.round(Float.parseFloat(value)));
        } catch (NumberFormatException e) {
            logger.warn("数量转换失败，原值: {}", value, e);
            return value; // 转换失败时返回原值
        }
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
