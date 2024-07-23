package com.br.marketing.entity;

import java.util.Date;

public class PeriodPushLog {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 以逗号分隔的待处理数据id
     */
    private String ids;

    /**
     * 枚举TransferSource的code,1通用转化流程,2客服拨打数据,3原始数据上传流程,4人工拨打流程,5转化数据集合流程,6初始数据集合流程
     */
    private Integer source;

    /**
     * 执行状态 0待推送、1推送中、2推送成功、3推送失败、4推送超时、5数据不满足条件不推送
     */
    private Integer status;

    /**
     * 失败条数
     */
    private Integer failNum;

    /**
     * 推送条数
     */
    private Integer pushNum;

    /**
     * 逻辑删除 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 待推送数据入库时间
     */
    private Date createTime;

    /**
     * 更新记录时间
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

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids == null ? null : ids.trim();
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getFailNum() {
        return failNum;
    }

    public void setFailNum(Integer failNum) {
        this.failNum = failNum;
    }

    public Integer getPushNum() {
        return pushNum;
    }

    public void setPushNum(Integer pushNum) {
        this.pushNum = pushNum;
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