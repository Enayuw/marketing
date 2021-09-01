package com.br.marketing.entity;

import java.util.Date;

public class MarketingTaskExtend {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * b_marketing_task表id
     */
    private Long taskId;

    /**
     * 
     */
    private String cusTaskId;

    /**
     * 场景
     */
    private String groupType;

    /**
     * 1-有效；9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * 扩展表头字段
     */
    private String extendShowTitle;

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getCusTaskId() {
        return cusTaskId;
    }

    public void setCusTaskId(String cusTaskId) {
        this.cusTaskId = cusTaskId == null ? null : cusTaskId.trim();
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType == null ? null : groupType.trim();
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

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime == null ? null : uploadTime.trim();
    }

    public String getExtendShowTitle() {
        return extendShowTitle;
    }

    public void setExtendShowTitle(String extendShowTitle) {
        this.extendShowTitle = extendShowTitle == null ? null : extendShowTitle.trim();
    }
}