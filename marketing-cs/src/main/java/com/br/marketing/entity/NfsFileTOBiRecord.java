package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class NfsFileTOBiRecord {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 文件类型
     *
     */
    private Integer fileType;

    /**
     * 1-转化提取文件同步至marketing-bi
     * 9-邮件读取任务的类型
     */
    private String busType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * b_transfer_file_task任务表id
     */
    private Long taskId;

    /**
     * 执行日期
     */
    private String executeDate;

    /**
     * 文件传输时间
     */
    private String sendTime;

    /**
     * 入库时间，推送时间
     */
    private Date createTime;

    /**
     * 更新记录时间
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

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(String executeDate) {
        this.executeDate = executeDate == null ? null : executeDate.trim();
    }
}