package com.br.marketing.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SmsChannelDto {

    @Schema(description = "渠道id")
    private Long channelId;

    @Schema(description = "渠道名称")
    private String channelName;
}
