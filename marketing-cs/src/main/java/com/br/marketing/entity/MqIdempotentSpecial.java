package com.br.marketing.entity;

import java.util.Date;

public class MqIdempotentSpecial {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 幂等键
     */
    private Long idempotentKey;

    /**
     * 客户编号
     */
    private String apiCode;

    /**
     * mq消息标签
     */
    private String tag;

    /**
     * 是否业务已执行完成 0-否，1-是
     */
    private Integer isFinished;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDeleted;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建日期
     */
    private Integer createDate;

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

    public Long getIdempotentKey() {
        return idempotentKey;
    }

    public void setIdempotentKey(Long idempotentKey) {
        this.idempotentKey = idempotentKey;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag == null ? null : tag.trim();
    }

    public Integer getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(Integer isFinished) {
        this.isFinished = isFinished;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
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