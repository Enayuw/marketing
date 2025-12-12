package com.br.marketing.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SmsAccountDto {

    @Schema(description = "configId")
    private Long configId;

    @Schema(description = "groupId")
    private Long groupId;

    @Schema(description = "供应商id")
    @NotNull(message = "供应商id不能为空")
    private Long vendorId;

    @Schema(description = "供应商名称")
    @NotEmpty(message = "供应商名称不能为空")
    private String vendorName;

    @Schema(description = "渠道信息")
    @NotEmpty(message = "渠道信息不能为空")
    private List<SmsChannelDto> channels;

    @Schema(description = "价格信息")
    @NotEmpty(message = "价格信息不能为空")
    private List<PriceDateDTO> priceDates;
}
