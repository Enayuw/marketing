package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LineCallerDto {

    @ApiModelProperty(value = "线路id")
    private Long gatewayId;

    @ApiModelProperty(value = "主叫项目名称")
    private String callerFullname;
}
