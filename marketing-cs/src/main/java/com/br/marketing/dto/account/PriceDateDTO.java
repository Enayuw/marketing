package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceDateDTO {

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "生效开始日期")
    private LocalDate effectStartDate;
}
