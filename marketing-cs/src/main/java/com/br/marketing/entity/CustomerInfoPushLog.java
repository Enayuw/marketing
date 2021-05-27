package com.br.marketing.entity;

import java.util.Date;

public class CustomerInfoPushLog {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 任务id
     */
    private Long mId;

    /**
     * 发送的唯一键（任务id+自增值）
     */
    private String batch;

    /**
     * 结果值
     */
    private String resultContent;

    /**
     * http状态码
     */
    private String httpStatus;

    /**
     * 返回的状态码
     */
    private String code;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 
     */
    private String errorContent;

    /**
     * 1-不需查询；2-需要去查询；3-更新中；4-已入库；5-失败入库；
     */
    private Integer realStauts;

    /**
     * 
     */
    private String param;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getmId() {
        return mId;
    }

    public void setmId(Long mId) {
        this.mId = mId;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch == null ? null : batch.trim();
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent == null ? null : resultContent.trim();
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus == null ? null : httpStatus.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getErrorContent() {
        return errorContent;
    }

    public void setErrorContent(String errorContent) {
        this.errorContent = errorContent == null ? null : errorContent.trim();
    }

    public Integer getRealStauts() {
        return realStauts;
    }

    public void setRealStauts(Integer realStauts) {
        this.realStauts = realStauts;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param == null ? null : param.trim();
    }
}