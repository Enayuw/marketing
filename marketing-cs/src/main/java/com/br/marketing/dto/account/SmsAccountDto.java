package com.br.marketing.dto.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SmsAccountDto {

    @ApiModelProperty(value = "configId")
    private Long configId;

    @ApiModelProperty(value = "groupId")
    private Long groupId;

    @ApiModelProperty(value = "供应商id")
    @NotNull(message = "供应商id不能为空")
    private Long vendorId;

    @ApiModelProperty(value = "供应商名称")
    @NotEmpty(message = "供应商名称不能为空")
    private String vendorName;

    @ApiModelProperty(value = "渠道信息")
    @NotEmpty(message = "渠道信息不能为空")
    private List<SmsChannelDto> channels;

    @ApiModelProperty(value = "价格信息")
    @NotEmpty(message = "价格信息不能为空")
    private List<PriceDateDTO> priceDates;
}
