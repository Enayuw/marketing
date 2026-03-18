package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建/更新模板请求
 * 
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Schema(description = "创建/更新模板请求")
public class CreateTemplateRequest {

    @Schema(description = "模板ID（更新时需要）")
    private Long id;

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "状态：0-禁用 1-启用")
    private Byte status;

    @Schema(description = "链路图结构（JSON格式，包含节点、连线、位置等完整信息）")
    private String graphJson;

    @Schema(description = "节点列表")
    @Valid
    private List<TemplateNodeVO> nodes;

    @Schema(description = "边列表")
    @Valid
    private List<TemplateEdgeVO> edges;
}
