package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 链路列表查询请求
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Schema(description = "链路列表查询请求")
public class LinkListRequest {

    @Schema(description = "链路代码（模糊查询）")
    private String linkCode;

    @Schema(description = "链路名称（模糊查询）")
    private String linkName;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Integer status;

    @Schema(description = "来源类型：MANUAL-手动创建 AUTO-自动发现")
    private String sourceType;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页大小")
    private Integer pageSize;
}


