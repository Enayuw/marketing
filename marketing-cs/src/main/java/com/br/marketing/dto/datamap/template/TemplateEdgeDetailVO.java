package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板边详情VO（用于查询返回）
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板边详情")
public class TemplateEdgeDetailVO {

    @Schema(description = "边ID（数据库主键）")
    private Long id;

    @Schema(description = "起始节点ID（数据库ID，对应TemplateNodeDetailVO.id）")
    private Long fromNodeId;

    @Schema(description = "目标节点ID（数据库ID，对应TemplateNodeDetailVO.id）")
    private Long toNodeId;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;

    @Schema(description = "边描述")
    private String description;
}
