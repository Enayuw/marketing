package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;



public class VariableAllocationVO {

    @ApiModelProperty(value = "任务流水号")
    private Long id;

    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @ApiModelProperty(value = "配置类型")
    private String allocationType;

    @ApiModelProperty(value = "撞得总量级")
    private int normalQuantity;


    @ApiModelProperty(value = "异常总量级")
    private int abnormalQuantity;

    @ApiModelProperty(value = "即将撞库量级")
    private int releaseTimeNum;

    @ApiModelProperty(value = "可补充的量级")
    private int falseNum;

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

    public int getNormalQuantity() {
        return normalQuantity;
    }

    public void setNormalQuantity(int normalQuantity) {
        this.normalQuantity = normalQuantity;
    }

    public int getAbnormalQuantity() {
        return abnormalQuantity;
    }

    public void setAbnormalQuantity(int abnormalQuantity) {
        this.abnormalQuantity = abnormalQuantity;
    }

    public int getReleaseTimeNum() {
        return releaseTimeNum;
    }

    public void setReleaseTimeNum(int releaseTimeNum) {
        this.releaseTimeNum = releaseTimeNum;
    }

    public int getFalseNum() {
        return falseNum;
    }

    public void setFalseNum(int falseNum) {
        this.falseNum = falseNum;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }
}
