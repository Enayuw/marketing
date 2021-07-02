package com.br.marketing.entity;

public class MarketingCustomer {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 备注
     */
    private String message;

    /**
     * incr 增量、all 全量、once 一次
     */
    private String type;

    /**
     * 并发数
     */
    private Integer threadNum;

    /**
     * 跑数时间 1实时跑，2 T+1
     */
    private Byte taskTime;

    /**
     * 状态 1正常，0删除
     */
    private Byte status;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Byte getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(Byte taskTime) {
        this.taskTime = taskTime;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}