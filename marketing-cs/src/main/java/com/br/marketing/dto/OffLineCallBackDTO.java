package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OffLineCallBackDTO {
    @Schema(description = "请求id")
    @NotNull(message = "requestId不能为空")
    private String requestId;

    @Schema(description = "文件路径")
    @NotNull(message = "filePath不能为空")
    private String filePath;

    @Schema(description = "文件名称")
    @NotNull(message = "fileName不能为空")
    private String fileName;

    @Schema(description = "状态 fail | success")
    @NotNull(message = "status不能为空")
    private String status;
}
