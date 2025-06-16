package com.br.marketing.entity;

import java.util.Date;

public class DataExportTask {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 数据源名称(marketing_tikv/marketing_bi等)
     */
    private String dataSource;

    /**
     * 导出表头(逗号分隔)
     */
    private String exportHeaders;

    /**
     * 字段映射关系JSON
     */
    private String fieldMapping;

    /**
     * 查询条件配置JSON
     */
    private String queryCondition;

    /**
     * 预估数据量
     */
    private Long estimatedRows;

    /**
     * 导出文件名
     */
    private String fileNameTemplate;

    /**
     * 任务规则
     */
    private String taskRule;

    /**
     * 状态:1-启用,0-禁用
     */
    private Byte status;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource == null ? null : dataSource.trim();
    }

    public String getExportHeaders() {
        return exportHeaders;
    }

    public void setExportHeaders(String exportHeaders) {
        this.exportHeaders = exportHeaders == null ? null : exportHeaders.trim();
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping == null ? null : fieldMapping.trim();
    }

    public String getQueryCondition() {
        return queryCondition;
    }

    public void setQueryCondition(String queryCondition) {
        this.queryCondition = queryCondition == null ? null : queryCondition.trim();
    }

    public Long getEstimatedRows() {
        return estimatedRows;
    }

    public void setEstimatedRows(Long estimatedRows) {
        this.estimatedRows = estimatedRows;
    }

    public String getFileNameTemplate() {
        return fileNameTemplate;
    }

    public void setFileNameTemplate(String fileNameTemplate) {
        this.fileNameTemplate = fileNameTemplate == null ? null : fileNameTemplate.trim();
    }

    public String getTaskRule() {
        return taskRule;
    }

    public void setTaskRule(String taskRule) {
        this.taskRule = taskRule == null ? null : taskRule.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy == null ? null : createBy.trim();
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
    }
}