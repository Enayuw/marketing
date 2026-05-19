package com.br.marketing.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Schema(description = "校验产管系统模型")
public class MarketingTaskModelCheck {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商户编号")
    private String apiCode;

    @Schema(description = "批次号")
    private String batchNumber;

    @Schema(description = "规则id")
    private String cusBatch;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则简拼")
    private String ruleNameShort;

    @Schema(description = "产管系统模型校验状态 0-未通过 1-通过")
    private Integer modelCheckStatus;

    @Schema(description = "未通过的模型中英文名称和版本，JSON存储")
    private String failedModelInfo;

    @Schema(description = "删除标志；1-正常；9-删除；", hidden = true)
    private Integer isDel;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间", hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch == null ? null : cusBatch.trim();
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName == null ? null : ruleName.trim();
    }

    public String getRuleNameShort() {
        return ruleNameShort;
    }

    public void setRuleNameShort(String ruleNameShort) {
        this.ruleNameShort = ruleNameShort == null ? null : ruleNameShort.trim();
    }

    public Integer getModelCheckStatus() {
        return modelCheckStatus;
    }

    public void setModelCheckStatus(Integer modelCheckStatus) {
        this.modelCheckStatus = modelCheckStatus;
    }

    public String getFailedModelInfo() {
        return failedModelInfo;
    }

    public void setFailedModelInfo(String failedModelInfo) {
        this.failedModelInfo = failedModelInfo == null ? null : failedModelInfo.trim();
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
