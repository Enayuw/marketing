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
     * finish或success文件生成时间，默认为1实时,2表示定时,
     */
    private Byte finishDate;

    /**
     * 是否推送客服,1推送,0不推送
     */
    private Byte pushCustomer;

    /**
     * 是否校验黑名单,1校验,0不校验
     */
    private Byte checkBlackList;

    /**
     * 是否校验条数,1校验,0不校验
     */
    private Byte checkRedisNumber;

    /**
     * 是否记录日志,1记录,0不记录
     */
    private Byte saveLog;

    /**
     * 跑分顺序根据此字段倒序排序
     */
    private Byte sort;

    /**
     * 状态 1正常，0删除
     */
    private Byte status;

    /**
     * 扩展字段
     */
    private String extendConfigInfo;

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

    public Byte getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Byte finishDate) {
        this.finishDate = finishDate;
    }

    public Byte getPushCustomer() {
        return pushCustomer;
    }

    public void setPushCustomer(Byte pushCustomer) {
        this.pushCustomer = pushCustomer;
    }

    public Byte getCheckBlackList() {
        return checkBlackList;
    }

    public void setCheckBlackList(Byte checkBlackList) {
        this.checkBlackList = checkBlackList;
    }

    public Byte getCheckRedisNumber() {
        return checkRedisNumber;
    }

    public void setCheckRedisNumber(Byte checkRedisNumber) {
        this.checkRedisNumber = checkRedisNumber;
    }

    public Byte getSaveLog() {
        return saveLog;
    }

    public void setSaveLog(Byte saveLog) {
        this.saveLog = saveLog;
    }

    public Byte getSort() {
        return sort;
    }

    public void setSort(Byte sort) {
        this.sort = sort;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getExtendConfigInfo() {
        return extendConfigInfo;
    }

    public void setExtendConfigInfo(String extendConfigInfo) {
        this.extendConfigInfo = extendConfigInfo == null ? null : extendConfigInfo.trim();
    }
}