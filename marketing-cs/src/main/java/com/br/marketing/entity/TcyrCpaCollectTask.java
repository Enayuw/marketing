package com.br.marketing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaCollectTask {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 来源id
     */
    private Long sourceId;

    /**
     * 来源类型 1-撞库成功；2-撞库失败；3-大空白组
     */
    private Integer sourceType;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 任务状态 0-未执行；1-执行中；2-执行完成
     */
    private Integer status;

    /**
     * 扩展字段
     */
    private String extend;

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

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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