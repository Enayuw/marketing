package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;



public class VariableAllocationVO {

    @ApiModelProperty(value = "任务流水号")
    private Long id;

    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @ApiModelProperty(value = "配置类型")
    private String allocationType;

    @ApiModelProperty(value = "配置值")
    private String allocationValue;

    @ApiModelProperty(value = "撞得总量级")
    private Integer normalQuantity;


    @ApiModelProperty(value = "异常总量级")
    private Integer abnormalQuantity;

    @ApiModelProperty(value = "即将撞库量级")
    private Integer releaseTimeNum;

    @ApiModelProperty(value = "可补充的量级")
    private Integer falseNum;

    @ApiModelProperty(value = "请求时间")
    private String requestTime;

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
        this.apiCode = apiCode;
    }

    public String getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(String allocationType) {
        this.allocationType = allocationType;
    }

    public String getAllocationValue() {
        return allocationValue;
    }

    public void setAllocationValue(String allocationValue) {
        this.allocationValue = allocationValue;
    }

    public Integer getNormalQuantity() {
        return normalQuantity;
    }

    public void setNormalQuantity(Integer normalQuantity) {
        this.normalQuantity = normalQuantity;
    }

    public Integer getAbnormalQuantity() {
        return abnormalQuantity;
    }

    public void setAbnormalQuantity(Integer abnormalQuantity) {
        this.abnormalQuantity = abnormalQuantity;
    }

    public Integer getReleaseTimeNum() {
        return releaseTimeNum;
    }

    public void setReleaseTimeNum(Integer releaseTimeNum) {
        this.releaseTimeNum = releaseTimeNum;
    }

    public Integer getFalseNum() {
        return falseNum;
    }

    public void setFalseNum(Integer falseNum) {
        this.falseNum = falseNum;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    @Override
    public String toString() {
        return "VariableAllocationVO{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", allocationType='" + allocationType + '\'' +
                ", allocationValue='" + allocationValue + '\'' +
                ", normalQuantity=" + normalQuantity +
                ", abnormalQuantity=" + abnormalQuantity +
                ", releaseTimeNum=" + releaseTimeNum +
                ", falseNum=" + falseNum +
                ", requestTime='" + requestTime + '\'' +
                '}';
    }
}
