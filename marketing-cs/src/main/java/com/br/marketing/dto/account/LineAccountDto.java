package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import java.util.List;

@Data
public class LineAccountDto {

    @ApiModelProperty(value = "configId")
    private Long configId;

    @ApiModelProperty(value = "供应商名称")
    @NotEmpty(message = "供应商名称不能为空")
    private String lineSupplier;

    @ApiModelProperty(value = "线路信息")
    @NotEmpty(message = "线路信息不能为空")
    private List<LineCallerDto> lines;

    @ApiModelProperty(value = "价格信息")
    @NotEmpty(message = "价格信息不能为空")
    private List<PriceDateDTO> priceDates;
}
