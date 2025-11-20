package com.br.marketing.dto.tccpa;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TcCpDataPackageVO {

    @ApiModelProperty(value = "数据包名称")
    @NotNull(message = "数据包名称不能为空")
    private String packageName;

    @ApiModelProperty(value = "数据包状态")
    private Integer status;

}
