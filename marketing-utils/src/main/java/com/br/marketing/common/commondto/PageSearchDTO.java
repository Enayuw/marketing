package com.br.marketing.common.commondto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "列表查询条件")
public class PageSearchDTO {

    @ApiModelProperty(value = "当前页码")
    @NotNull(message = "页码不能为空")
    private Integer current;

    @ApiModelProperty(value = "页容量")
    private Integer size;
}
