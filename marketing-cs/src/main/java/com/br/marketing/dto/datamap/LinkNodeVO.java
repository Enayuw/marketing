package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 链路节点 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@ApiModel("链路节点信息")
public class LinkNodeVO {

    @ApiModelProperty("节点字典ID")
    private Long nodeDictId;

    @ApiModelProperty("节点顺序（从1开始）")
    private Integer nodeOrder;

    @ApiModelProperty("节点别名（在链路中的显示名称）")
    private String nodeAlias;

    @ApiModelProperty("源节点ID列表（逗号分隔，第一个节点为空）")
    private String nodeSourceId;

    @ApiModelProperty("目标节点ID列表（逗号分隔）")
    private String nodeTargetId;
}


