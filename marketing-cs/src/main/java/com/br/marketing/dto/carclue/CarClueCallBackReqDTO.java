package com.br.marketing.dto.carclue;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CarClueCallBackReqDTO {
    @ApiModelProperty(value = "线索id")
    private String orderId;
    @ApiModelProperty(value = "推送状态 1：成功 2：失败")
    private Integer pushState;
    @ApiModelProperty(value = "推送状态 1：成功 2：失败")
    private Integer finalState;
    @ApiModelProperty(value = "返回信息")
    private String message;
}
