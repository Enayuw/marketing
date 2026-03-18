package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 模板列表项 VO
 * 
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板列表项")
public class TemplateListItemVO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Byte status;

    @Schema(description = "节点数量")
    private Integer nodeCount;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
