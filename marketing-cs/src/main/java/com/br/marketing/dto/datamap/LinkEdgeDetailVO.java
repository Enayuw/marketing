package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 链路边详情 VO（响应用）
 *
 * @author bingxu.kong
 * @since 2025/01/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "链路边详情")
public class LinkEdgeDetailVO {

    @Schema(description = "边ID")
    private Long id;

    @Schema(description = "链路ID")
    private Long linkId;

    @Schema(description = "起始节点ID（关联biz_tracking_link_node.id）")
    private Long fromNodeId;

    @Schema(description = "目标节点ID（关联biz_tracking_link_node.id）")
    private Long toNodeId;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;

    @Schema(description = "边描述")
    private String description;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;
}
