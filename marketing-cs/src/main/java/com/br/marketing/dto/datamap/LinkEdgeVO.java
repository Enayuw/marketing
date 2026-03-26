package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 链路边 VO（请求用）
 *
 * @author bingxu.kong
 * @since 2025/01/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "链路边信息")
public class LinkEdgeVO {

    @Schema(description = "起始节点临时ID（对应LinkNodeVO.nodeId，前端生成）")
    private Long sourceNodeId;

    @Schema(description = "目标节点临时ID（对应LinkNodeVO.nodeId，前端生成）")
    private Long targetNodeId;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;

    @Schema(description = "边描述")
    private String description;
}
