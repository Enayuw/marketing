package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 链路列表项 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@Schema(description = "链路列表项")
public class LinkListItemVO {

    @Schema(description = "链路ID")
    private Long id;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "链路代码")
    private String linkCode;

    @Schema(description = "链路名称")
    private String linkName;

    @Schema(description = "业务场景")
    private String bizScene;

    @Schema(description = "链路描述")
    private String description;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Integer status;

    @Schema(description = "来源类型：MANUAL-手动创建 AUTO-自动发现")
    private String sourceType;

    @Schema(description = "匹配的模板ID")
    private String templateId;

    @Schema(description = "节点数量")
    private Integer nodeCount;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;
}


