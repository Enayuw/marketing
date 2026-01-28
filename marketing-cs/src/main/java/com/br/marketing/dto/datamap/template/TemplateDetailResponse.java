package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 模板详情响应
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板详情响应")
public class TemplateDetailResponse {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Byte status;

    @Schema(description = "链路图结构（JSON格式，包含节点、连线、位置等完整信息）")
    private String graphJson;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "节点列表")
    private List<TemplateNodeDetailVO> nodes;

    @Schema(description = "边列表")
    private List<TemplateEdgeDetailVO> edges;
}
