package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaMagnitudeDistDTO {

    @Schema(description = "releaseTime")
    private String releaseTime;

    @Schema(description = "被友商锁定量级")
    private Long lockByOtrNum;

    @Schema(description = "空白组量级")
    private Long blankNum;
}
