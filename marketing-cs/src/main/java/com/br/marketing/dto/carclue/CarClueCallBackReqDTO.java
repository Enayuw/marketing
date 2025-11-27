package com.br.marketing.dto.carclue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CarClueCallBackReqDTO {
    @Schema(description = "线索id")
    private String orderId;
    @Schema(description = "推送状态 1：成功 2：失败")
    private Integer pushState;
    @Schema(description = "推送状态 1：成功 2：失败")
    private Integer finalState;
    @Schema(description = "返回信息")
    private String message;
}
