package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LineOutboundDto {

    @ApiModelProperty(value = "线路id")
    private Long gatewayId;

    @ApiModelProperty(value = "主叫号码")
    private String outboundNumber;
}
