package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OptConditionDTO{
    @ApiModelProperty(value = "条件id")
    @NotNull(message = "id不能为空")
    private Long id;

    @ApiModelProperty(value = "状态 1-开启；2-关闭")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
