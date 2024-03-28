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

    @ApiModelProperty(value = "页号")
    private Integer current;

    @ApiModelProperty(value = "页大小")
    private Integer size;

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

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
