package com.br.marketing.entity;

import java.util.Date;

public class UMengTimingTask {
    /**
     * 
     */
    private Long id;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 
     */
    private String apiCode;

    /**
     * 创建任务成功，友盟返回的任务id
     */
    private String umengTaskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除状态 1-正常 9删除
     */
    private Integer isDel;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getUmengTaskId() {
        return umengTaskId;
    }

    public void setUmengTaskId(String umengTaskId) {
        this.umengTaskId = umengTaskId == null ? null : umengTaskId.trim();
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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
}