package com.br.marketing.dto.linkgo;

/**
 * 创建任务基础数据传输对象
 * 用于接收marketingkit_cn项目组装好的基础任务数据
 * 文件名生成、序号生成等操作在marketing项目中完成
 * 
 * @author system
 * @date 2025/01/17
 */
public class CreateTaskDataDTO {

    private String ruleCode;
    private String dataSource;
    private String exportHeaders;
    private String fieldMapping;
    private String queryCondition;
    private Long estimatedRows;
    private String apiCode;
    private String userName;

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getExportHeaders() {
        return exportHeaders;
    }

    public void setExportHeaders(String exportHeaders) {
        this.exportHeaders = exportHeaders;
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public String getQueryCondition() {
        return queryCondition;
    }

    public void setQueryCondition(String queryCondition) {
        this.queryCondition = queryCondition;
    }

    public Long getEstimatedRows() {
        return estimatedRows;
    }

    public void setEstimatedRows(Long estimatedRows) {
        this.estimatedRows = estimatedRows;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "CreateTaskDataDTO{" +
                "ruleCode='" + ruleCode + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", exportHeaders='" + exportHeaders + '\'' +
                ", fieldMapping='" + fieldMapping + '\'' +
                ", queryCondition='" + queryCondition + '\'' +
                ", estimatedRows=" + estimatedRows +
                ", apiCode='" + apiCode + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
