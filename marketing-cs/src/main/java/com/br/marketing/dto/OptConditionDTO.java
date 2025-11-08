package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OptConditionDTO{
    @Schema(description = "条件id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态 1-开启；2-关闭")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
