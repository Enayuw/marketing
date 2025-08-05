package com.br.marketing.check.job.qifu.report.strategy;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

import java.util.List;

public interface ReportStrategy<T, M> {

    String getApiCode(JobExecutionMultipleShardingContext context);

    Integer getSubjectCode();

    List<T> queryData(String apiCode, String currentDate);

    List<M> convertToExcelModel(List<T> dataList);

    List<M> postProcess(List<M> excelList); // 可选，默认不处理

    Class<M> getExcelModelClass();

    String getSheetName();

    String getContent(String subject);

    String getAttachmentFileName(String subject);

}
