package com.br.marketing.common.commondto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "列表查询条件")
public class PageSearchDTO {

    @Schema(description = "当前页码")
    @NotNull(message = "页码不能为空")
    private Integer current;

    @Schema(description = "页容量")
    private Integer size;
}
