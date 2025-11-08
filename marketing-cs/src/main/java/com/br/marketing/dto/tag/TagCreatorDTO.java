package com.br.marketing.dto.tag;

import lombok.Data;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel("标签创建人DTO")
public class TagCreatorDTO {
    @ApiModelProperty("创建人ID")
    private Long userId;

    @ApiModelProperty("创建人名称")
    private String userName;
} 