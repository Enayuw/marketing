package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrCpaPushFileScript {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 场景
     */
    private String scene;

    /**
     * 提取脚本
     */
    private String extractScript;

    /**
     * 输出字段
     */
    private String outputField;

    /**
     * 数据源：1-tidb；2-doris
     */
    private Integer dataSource;

    /**
     * 执行优先级
     */
    private Integer priority;

    /**
     * 计划推送/生成日期（仅当天脚本会被任务读取）
     */
    private Date pushDate;

    /**
     * 1-正常；9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene == null ? null : scene.trim();
    }

    public String getExtractScript() {
        return extractScript;
    }

    public void setExtractScript(String extractScript) {
        this.extractScript = extractScript == null ? null : extractScript.trim();
    }

    public String getOutputField() {
        return outputField;
    }

    public void setOutputField(String outputField) {
        this.outputField = outputField == null ? null : outputField.trim();
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Date getPushDate() {
        return pushDate;
    }

    public void setPushDate(Date pushDate) {
        this.pushDate = pushDate;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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
}