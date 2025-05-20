package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CarClueChannelDTO {
    @NotNull(message = "Page number cannot be null")
    private Integer current;
    @NotNull(message = "Page size cannot be null")
    private Integer size;
    @ApiModelProperty(value = "品牌、车系、城市")
    private String search;
    @ApiModelProperty(value = "推送渠道")
    private String cluePushChannel;
}
