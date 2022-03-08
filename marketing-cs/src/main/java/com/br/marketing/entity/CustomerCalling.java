package com.br.marketing.entity;

import java.util.Date;

public class CustomerCalling {
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
     * sftp文件析出字段
     */
    private String columnsDetail;
    /**
     * 客户析出字段
     */
    private String apiColumnsDetail;

    /**
     * sftp密码
     */
    private String sftpPassword;

    /**
     * 扩展字段
     */
    private String extendConfigInfo;

    /**
     * 判断条件
     */
    private String conditions;

    /**
     * api推送并发数
     */
    private Integer pushThreadNum;

    /**
     * 跑分结果推送类型，0文件，1 api，默认支持文件推送
     */
    private Integer pushType;

    /**
     * 推送地址
     */
    private String pushUrl;

    /**
     * sftp 地址
     */
    private String sftpPath;

    /**
     * 用户名
     */
    private String sftpName;

    /**
     * 状态 1正常，0删除
     */
    private Byte status;

    /**
     * 更新时间
     */
    private Date updateTime;

    public String getApiColumnsDetail() {
        return apiColumnsDetail;
    }

    public void setApiColumnsDetail(String apiColumnsDetail) {
        this.apiColumnsDetail = apiColumnsDetail;
    }

    /**
     * 创建时间
     */


    private Date createTime;

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

    public String getColumnsDetail() {
        return columnsDetail;
    }

    public void setColumnsDetail(String columnsDetail) {
        this.columnsDetail = columnsDetail == null ? null : columnsDetail.trim();
    }

    public String getSftpPassword() {
        return sftpPassword;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword == null ? null : sftpPassword.trim();
    }

    public String getExtendConfigInfo() {
        return extendConfigInfo;
    }

    public void setExtendConfigInfo(String extendConfigInfo) {
        this.extendConfigInfo = extendConfigInfo == null ? null : extendConfigInfo.trim();
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions == null ? null : conditions.trim();
    }

    public Integer getPushThreadNum() {
        return pushThreadNum;
    }

    public void setPushThreadNum(Integer pushThreadNum) {
        this.pushThreadNum = pushThreadNum;
    }

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl == null ? null : pushUrl.trim();
    }

    public String getSftpPath() {
        return sftpPath;
    }

    public void setSftpPath(String sftpPath) {
        this.sftpPath = sftpPath == null ? null : sftpPath.trim();
    }

    public String getSftpName() {
        return sftpName;
    }

    public void setSftpName(String sftpName) {
        this.sftpName = sftpName == null ? null : sftpName.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CustomerCalling{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", message='" + message + '\'' +
                ", columnsDetail='" + columnsDetail + '\'' +
                ", sftpPassword='" + sftpPassword + '\'' +
                ", extendConfigInfo='" + extendConfigInfo + '\'' +
                ", conditions='" + conditions + '\'' +
                ", pushThreadNum=" + pushThreadNum +
                ", pushType=" + pushType +
                ", pushUrl='" + pushUrl + '\'' +
                ", sftpPath='" + sftpPath + '\'' +
                ", sftpName='" + sftpName + '\'' +
                ", status=" + status +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}