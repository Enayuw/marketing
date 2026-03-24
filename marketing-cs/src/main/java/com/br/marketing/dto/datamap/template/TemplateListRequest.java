package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板列表查询请求
 * 
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Schema(description = "模板列表查询请求")
public class TemplateListRequest {

    @Schema(description = "模板名称（模糊查询）")
    private String templateName;

    @Schema(description = "描述（模糊查询）")
    private String description;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Byte status;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页大小")
    private Integer pageSize;
}
