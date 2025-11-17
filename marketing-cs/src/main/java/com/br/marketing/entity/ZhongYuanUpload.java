package com.br.marketing.entity;

import java.util.Date;

public class ZhongYuanUpload {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 全局唯一流水号
     */
    private String flowid;

    /**
     * 系统标识，默认cs
     */
    private String sysid;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 版本号，默认1.0
     */
    private String version;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 批次名称
     */
    private String batchname;

    /**
     * 批次编号
     */
    private String batchno;

    /**
     * 开始时间
     */
    private String starttime;

    /**
     * 结束时间
     */
    private String endtime;

    /**
     * 节假日禁止标识
     */
    private String festivalban;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 上报结束标识
     */
    private String reportendflag;

    /**
     * 任务数据列表（JSON格式）
     */
    private String taskdatalist;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getFlowid() {
        return flowid;
    }

    public void setFlowid(String flowid) {
        this.flowid = flowid == null ? null : flowid.trim();
    }

    public String getSysid() {
        return sysid;
    }

    public void setSysid(String sysid) {
        this.sysid = sysid == null ? null : sysid.trim();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp == null ? null : timestamp.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getBatchname() {
        return batchname;
    }

    public void setBatchname(String batchname) {
        this.batchname = batchname == null ? null : batchname.trim();
    }

    public String getBatchno() {
        return batchno;
    }

    public void setBatchno(String batchno) {
        this.batchno = batchno == null ? null : batchno.trim();
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime == null ? null : starttime.trim();
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime == null ? null : endtime.trim();
    }

    public String getFestivalban() {
        return festivalban;
    }

    public void setFestivalban(String festivalban) {
        this.festivalban = festivalban == null ? null : festivalban.trim();
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority == null ? null : priority.trim();
    }

    public String getReportendflag() {
        return reportendflag;
    }

    public void setReportendflag(String reportendflag) {
        this.reportendflag = reportendflag == null ? null : reportendflag.trim();
    }

    public String getTaskdatalist() {
        return taskdatalist;
    }

    public void setTaskdatalist(String taskdatalist) {
        this.taskdatalist = taskdatalist == null ? null : taskdatalist.trim();
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