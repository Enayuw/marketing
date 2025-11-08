package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class VariableAllocationDTO {

    @Schema(description = "apicode")
    private String apiCode;

    @Schema(description = "配置类型")
    private String allocationType;

    @Schema(description = "请求时间")
    private String requestTime;


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

}
