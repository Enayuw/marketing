package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
public class MarketingPreUserSyncStatusDTO {

    @NotNull(message = "apiCode不能为空")
    @NotEmpty(message = "apiCode不能为空")
    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @NotNull(message = "taskId不能为空")
    @NotEmpty(message = "taskId不能为空")
    @ApiModelProperty(value = "任务id")
    private String taskId;

    @NotNull(message = "requestId不能为空")
    @NotEmpty(message = "requestId不能为空")
    @ApiModelProperty(value = "请求批次id")
    private String requestId;
}
