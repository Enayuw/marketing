package com.br.marketing.entity;

import java.util.Date;

public class UMengData {
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
     * sftp idcard
     */
    private String idCard;

    /**
     * phone sha256加密
     */
    private String cell;

    /**
     * sft原字段 cust_num
     */
    private String cusNum;

    /**
     * sft原字段
     */
    private String usertype;

    /**
     * name
     */
    private String name;

    /**
     * sftp字段
     */
    private String pdCellType;

    /**
     * sftp字段
     */
    private String pdCellProvince;

    /**
     * 友盟设备注册请求状态 -1:请求注册失败 1:未请求 2:请求注册成功
     */
    private Integer deviceAddStatus;

    /**
     * 状态-1:推送失败 1-未推送；2-推送
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * extend信息
     */
    private String extend;

    /**
     * data_message
     */
    private String dataMessage;

    /**
     * create_date
     */
    private String createDate;

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

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard == null ? null : idCard.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getCusNum() {
        return cusNum;
    }

    public void setCusNum(String cusNum) {
        this.cusNum = cusNum == null ? null : cusNum.trim();
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype == null ? null : usertype.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPdCellType() {
        return pdCellType;
    }

    public void setPdCellType(String pdCellType) {
        this.pdCellType = pdCellType == null ? null : pdCellType.trim();
    }

    public String getPdCellProvince() {
        return pdCellProvince;
    }

    public void setPdCellProvince(String pdCellProvince) {
        this.pdCellProvince = pdCellProvince == null ? null : pdCellProvince.trim();
    }

    public Integer getDeviceAddStatus() {
        return deviceAddStatus;
    }

    public void setDeviceAddStatus(Integer deviceAddStatus) {
        this.deviceAddStatus = deviceAddStatus;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
    }
}