package com.br.marketing.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LineCallerDto {

    @Schema(description = "线路id")
    private Long gatewayId;

    @Schema(description = "主叫项目名称")
    private String callerFullname;
}
