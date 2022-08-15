package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OffLineCallBackDTO {
    @ApiModelProperty(value = "请求id")
    @NotNull(message = "requestId不能为空")
    private String requestId;

    @ApiModelProperty(value = "文件路径")
    @NotNull(message = "filePath不能为空")
    private String filePath;

    @ApiModelProperty(value = "文件名称")
    @NotNull(message = "fileName不能为空")
    private String fileName;

    @ApiModelProperty(value = "状态 fail | success")
    @NotNull(message = "status不能为空")
    private String status;
}
