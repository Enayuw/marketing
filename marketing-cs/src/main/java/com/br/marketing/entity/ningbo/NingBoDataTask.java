package com.br.marketing.entity.ningbo;

import java.util.Date;

public class NingBoDataTask {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务执行日期 (T日)，格式: yyyy-MM-dd
     */
    private Date taskDate;

    /**
     * 任务类型：1-下载任务，2-上传任务
     */
    private Integer taskType;

    /**
     * 任务状态：0-待执行，1-执行中，2-执行成功，3-执行失败
     */
    private Integer status;

    /**
     * 任务执行结果或错误信息
     */
    private String resultMessage;

    /**
     * 记录创建时间
     */
    private Date createTime;

    /**
     * 记录最后更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(Date taskDate) {
        this.taskDate = taskDate;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage == null ? null : resultMessage.trim();
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