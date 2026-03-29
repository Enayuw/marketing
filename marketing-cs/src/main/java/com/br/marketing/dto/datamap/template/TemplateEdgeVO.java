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

    @Schema(description = "起始节点临时ID（对应TemplateNodeVO.tempId）")
    private String sourceNodeTempId;

    @Schema(description = "目标节点临时ID（对应TemplateNodeVO.tempId）")
    private String targetNodeTempId;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;

    @Schema(description = "边描述")
    private String description;
}
