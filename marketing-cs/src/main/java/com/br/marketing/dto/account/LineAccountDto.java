package com.br.marketing.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import java.util.List;

@Data
public class LineAccountDto {

    @Schema(description = "configId")
    private Long configId;


    @Schema(description = "groupId")
    private Long groupId;

    @Schema(description = "供应商名称")
    @NotEmpty(message = "供应商名称不能为空")
    private String lineSupplier;

    @Schema(description = "线路信息")
    @NotEmpty(message = "线路信息不能为空")
    private List<LineCallerDto> lines;

    @Schema(description = "价格信息")
    @NotEmpty(message = "价格信息不能为空")
    private List<PriceDateDTO> priceDates;
}
