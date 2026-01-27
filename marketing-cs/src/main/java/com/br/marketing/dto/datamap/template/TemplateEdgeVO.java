package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板边VO
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板边信息")
public class TemplateEdgeVO {

    @Schema(description = "边ID（主键）")
    private Long id;

    @Schema(description = "起始节点ID（关联biz_tracking_template_node.id）")
    private Long fromNodeId;

    @Schema(description = "目标节点ID（关联biz_tracking_template_node.id）")
    private Long toNodeId;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;

    @Schema(description = "边描述")
    private String description;
}
