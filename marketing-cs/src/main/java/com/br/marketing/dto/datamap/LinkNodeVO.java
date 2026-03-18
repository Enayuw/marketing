package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 链路节点 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Schema(description = "链路节点信息")
public class LinkNodeVO {

    @Schema(description = "节点id 前端生成")
    private Long nodeId;

    @Schema(description = "节点字典ID")
    private Long nodeDictId;

    @Schema(description = "节点别名（在链路中的显示名称）")
    private String nodeAlias;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;
}


