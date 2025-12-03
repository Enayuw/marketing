package com.br.marketing.dto.tc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaMagnitudeDistDTO {

    @ApiModelProperty(value = "releaseTime")
    private String releaseTime;

    @ApiModelProperty(value = "被友商锁定量级")
    private Long lockByOtrNum;

    @ApiModelProperty(value = "空白组量级")
    private Long blankNum;
}
