package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrSync {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 用户唯一编号
     */
    private String userKey;

    /**
     * 终端 0-APP；1-小程序
     */
    private Integer terminal;

    /**
     * 电话
     */
    private String cell;

    /**
     * 是否匹配 0-否；1-是
     */
    private Integer isMatch;

    /**
     * 清洗状态 0-待清洗；1-清洗完成2:上传清洗失败 3:推送清洗失败
     */
    private Integer isClean;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     *  0:异常数据 1:正常数据
     */
    private Integer status;

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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey == null ? null : userKey.trim();
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public Integer getIsMatch() {
        return isMatch;
    }

    public void setIsMatch(Integer isMatch) {
        this.isMatch = isMatch;
    }

    public Integer getIsClean() {
        return isClean;
    }

    public void setIsClean(Integer isClean) {
        this.isClean = isClean;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}