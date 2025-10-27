package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 创建链路响应
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@ApiModel("创建链路响应")
public class CreateLinkResponse {

    @ApiModelProperty("链路ID")
    private Long linkId;

    @ApiModelProperty("链路代码")
    private String linkCode;

    @ApiModelProperty("节点数量")
    private Integer nodeCount;
}


