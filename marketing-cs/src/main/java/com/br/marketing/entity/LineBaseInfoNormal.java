package com.br.marketing.entity;

import java.util.Date;

public class LineBaseInfoNormal {
    /**
     * 
     */
    private Long id;

    /**
     * 线路id
     */
    private Long gatewayId;

    /**
     * 呼叫号码
     */
    private String caller;

    /**
     * 供应商组合id
     */
    private Long lineSupplierId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 外显号码
     */
    private String outboundNumber;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 状态 0-没修改 1-已修改 2-已删除
     */
    private Integer opeStatus;

    /**
     * 操作修改时间
     */
    private Date opeTime;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller == null ? null : caller.trim();
    }

    public Long getLineSupplierId() {
        return lineSupplierId;
    }

    public void setLineSupplierId(Long lineSupplierId) {
        this.lineSupplierId = lineSupplierId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName == null ? null : projectName.trim();
    }

    public String getOutboundNumber() {
        return outboundNumber;
    }

    public void setOutboundNumber(String outboundNumber) {
        this.outboundNumber = outboundNumber == null ? null : outboundNumber.trim();
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

    public Integer getOpeStatus() {
        return opeStatus;
    }

    public void setOpeStatus(Integer opeStatus) {
        this.opeStatus = opeStatus;
    }

    public Date getOpeTime() {
        return opeTime;
    }

    public void setOpeTime(Date opeTime) {
        this.opeTime = opeTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}