package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrCpaPushFileTask {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 场景
     */
    private String scene;

    /**
     * 本地路径
     */
    private String localPath;

    /**
     * 内部sftp路径
     */
    private String innerSftpPath;

    /**
     * 运营sftp路径
     */
    private String opeSftpPath;

    /**
     * 执行日期
     */
    private Date pushDate;

    /**
     * 任务状态 1-生成中；2-生成成功；3-传输至内部sftp；4-传输至运营sftp
     */
    private Integer status;

    /**
     * 文件总量级
     */
    private Integer total;

    /**
     * 提取详情
     */
    private String info;

    /**
     * 1-正常；9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
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

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene == null ? null : scene.trim();
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath == null ? null : localPath.trim();
    }

    public String getInnerSftpPath() {
        return innerSftpPath;
    }

    public void setInnerSftpPath(String innerSftpPath) {
        this.innerSftpPath = innerSftpPath == null ? null : innerSftpPath.trim();
    }

    public String getOpeSftpPath() {
        return opeSftpPath;
    }

    public void setOpeSftpPath(String opeSftpPath) {
        this.opeSftpPath = opeSftpPath == null ? null : opeSftpPath.trim();
    }

    public Date getPushDate() {
        return pushDate;
    }

    public void setPushDate(Date pushDate) {
        this.pushDate = pushDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
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
}