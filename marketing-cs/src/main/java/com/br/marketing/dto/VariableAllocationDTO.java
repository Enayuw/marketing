package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VariableAllocationDTO {

    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @ApiModelProperty(value = "配置类型")
    private String allocationType;

    @ApiModelProperty(value = "请求时间")
    private String requestTime;

    @ApiModelProperty(value = "请求结束时间")
    private String requestEndTime;


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

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestEndTime() {
        return requestEndTime;
    }

    public void setRequestEndTime(String requestEndTime) {
        this.requestEndTime = requestEndTime;
    }

}
