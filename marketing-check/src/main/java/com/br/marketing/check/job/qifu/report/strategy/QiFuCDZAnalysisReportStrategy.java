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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 360促动支分析报表策略
 * 在postProcess方法中实现单元格合并逻辑
 */
@Component("qiFuCDZAnalysisReportStrategy")
public class QiFuCDZAnalysisReportStrategy implements ReportStrategy<QifuActuation, QiFuCuDongAnalysisReportExcelModel>{

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
                    qifuActuation.setAppLoginRate(qifuActuation.getAppLoginRate().concat("%"));
                    qifuActuation.setUserLoanRate(qifuActuation.getUserLoanRate().concat("%"));
                    qifuActuation.setUserStartRate(qifuActuation.getUserStartRate().concat("%"));
                    QiFuCuDongAnalysisReportExcelModel reportExcelModel = new QiFuCuDongAnalysisReportExcelModel();
                    BeanUtils.copyProperties(qifuActuation, reportExcelModel);
                    return reportExcelModel;
                }).collect(Collectors.toList());
    }

    @Override
    public List<QiFuCuDongAnalysisReportExcelModel> postProcess(List<QiFuCuDongAnalysisReportExcelModel> excelList) {
        if (excelList == null || excelList.isEmpty()) {
            return excelList;
        }

        //按照月份 > 下发日期 > 用户类型对数据排序
        excelList.sort(Comparator
                .comparing(QiFuCuDongAnalysisReportExcelModel::getIssueMonth, Comparator.nullsFirst(String::compareTo))
                .thenComparing(QiFuCuDongAnalysisReportExcelModel::getIssueDate, Comparator.nullsFirst(String::compareTo))
                .thenComparing(QiFuCuDongAnalysisReportExcelModel::getUserType, Comparator.nullsFirst(String::compareTo)));

        List<QiFuCuDongAnalysisReportExcelModel> processedList = new ArrayList<>();
        
        for (int i = 0; i < excelList.size(); i++) {
            QiFuCuDongAnalysisReportExcelModel current = excelList.get(i);
            QiFuCuDongAnalysisReportExcelModel processed = new QiFuCuDongAnalysisReportExcelModel();
            BeanUtils.copyProperties(current, processed);
            
            // 处理月份列合并
            processed.setIssueMonth(processColumnMerge(excelList, i, "issueMonth"));
            
            // 处理下发日期列合并
            processed.setIssueDate(processColumnMerge(excelList, i, "issueDate"));
            
            // 处理用户类型列合并
            processed.setUserType(processColumnMerge(excelList, i, "userType"));
            
            // 为所有列添加居中样式标记
            processed.setSupplier(processed.getSupplier());
            processed.setValidDate(processed.getValidDate());
            processed.setCreditUserCount(processed.getCreditUserCount());
            processed.setAppLoginUserCount(processed.getAppLoginUserCount());
            processed.setStartUserCount(processed.getStartUserCount());
            processed.setUserLoanCount(processed.getUserLoanCount());
            processed.setAppLoginRate(processed.getAppLoginRate());
            processed.setUserStartRate(processed.getUserStartRate());
            processed.setUserLoanRate(processed.getUserLoanRate());
            
            processedList.add(processed);
        }
        
        return processedList;
    }

    /**
     * 处理列合并逻辑
     * 实现多级合并：月份 > 下发日期 > 用户类型
     */
    private String processColumnMerge(List<QiFuCuDongAnalysisReportExcelModel> dataList, int currentIndex, String fieldName) {
        if (currentIndex == 0) {
            // 第一行总是显示值
            return getFieldValue(dataList.get(currentIndex), fieldName);
        }
        
        String currentValue = getFieldValue(dataList.get(currentIndex), fieldName);
        String previousValue = getFieldValue(dataList.get(currentIndex - 1), fieldName);
        
        // 检查是否需要合并
        boolean shouldMerge = false;
        
        switch (fieldName) {
            case "issueMonth":
                // 月份列：只要相同就合并
                shouldMerge = currentValue.equals(previousValue);
                break;
                
            case "issueDate":
                // 下发日期列：月份相同且下发日期相同才合并
                shouldMerge = currentValue.equals(previousValue) && 
                             getFieldValue(dataList.get(currentIndex), "issueMonth").equals(
                             getFieldValue(dataList.get(currentIndex - 1), "issueMonth"));
                break;
                
            case "userType":
                // 用户类型列：月份相同、下发日期相同且用户类型相同才合并
                shouldMerge = currentValue.equals(previousValue) && 
                             getFieldValue(dataList.get(currentIndex), "issueMonth").equals(
                             getFieldValue(dataList.get(currentIndex - 1), "issueMonth")) &&
                             getFieldValue(dataList.get(currentIndex), "issueDate").equals(
                             getFieldValue(dataList.get(currentIndex - 1), "issueDate"));
                break;
        }
        
        // 如果需要合并，返回空字符串
        if (shouldMerge) {
            return "";
        }
        
        // 如果不需要合并，返回原值并添加居中样式
        return currentValue;
    }

    /**
     * 获取字段值
     */
    private String getFieldValue(QiFuCuDongAnalysisReportExcelModel data, String fieldName) {
        switch (fieldName) {
            case "issueMonth":
                return data.getIssueMonth();
            case "issueDate":
                return data.getIssueDate();
            case "userType":
                return data.getUserType();
            default:
                return "";
        }
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
}
