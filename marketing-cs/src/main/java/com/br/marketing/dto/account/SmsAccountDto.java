package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SmsAccountDto {

    @ApiModelProperty(value = "供应商id")
    private Integer vendorId;

    @ApiModelProperty(value = "供应商名称")
    private String vendorName;

    @ApiModelProperty(value = "渠道信息")
    private List<SmsChannelDto> channels;

    @ApiModelProperty(value = "价格信息")
    private List<PriceDateDTO> priceDates;
}
