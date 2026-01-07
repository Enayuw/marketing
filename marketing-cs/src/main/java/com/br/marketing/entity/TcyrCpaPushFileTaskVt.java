package com.br.marketing.entity;

import java.util.Date;
public class TcyrCpaPushFileTaskVt {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 撞库任务id
     */
    private String collidingTaskIds;

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
     * 送撞时间（多个撞库任务取最晚时间）
     */
    private Date pushTime;

    /**
     * 任务状态 1-生成中；2-生成成功；3-生成失败；4-传输至内部sftp；5-传输至运营sftp
     */
    private Integer status;

    /**
     * 文件总量级
     */
    private Integer total;

    /**
     * 1-正常；9-删除
     */
    private Integer isDel;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 提取详情
     */
    private String info;

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

    public String getCollidingTaskIds() {
        return collidingTaskIds;
    }

    public void setCollidingTaskIds(String collidingTaskIds) {
        this.collidingTaskIds = collidingTaskIds == null ? null : collidingTaskIds.trim();
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

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }
}