package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("标签创建人DTO")
public class TagCreatorDTO {
    @ApiModelProperty("创建人ID")
    private Long userId;

    @ApiModelProperty("创建人名称")
    private String userName;
} 