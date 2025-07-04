package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SmsChannelDto {

    @ApiModelProperty(value = "渠道id")
    private Integer channelId;

    @ApiModelProperty(value = "渠道名称")
    private String channelName;
}
