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
     * 同一批次下具体单个文件的id
     */
    private Long syncFileId;

    /**
     * 用户唯一编号
     */
    private String userKey;

    /**
     * 终端 0-APP；1-小程序 -1:空值 -2:特殊字符串解析异常
     */
    private String terminal;

    /**
     * 电话
     */
    private String cell;

    /**
     * 是否匹配 0-否；1-是; -1匹配中
     */
    private Integer isMatch;

    /**
     * 清洗状态 0-待清洗；1-清洗完成2:上传清洗失败 3:推送清洗失败 4:整体推送异常
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

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

    /**
     * 扩展字段
     */
    private String extend;

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

    public Long getSyncFileId() {
        return syncFileId;
    }

    public void setSyncFileId(Long syncFileId) {
        this.syncFileId = syncFileId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey == null ? null : userKey.trim();
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal == null ? null : terminal.trim();
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

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}