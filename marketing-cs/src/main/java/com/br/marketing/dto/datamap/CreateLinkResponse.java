package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 创建链路响应
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@Schema(description = "创建链路响应")
public class CreateLinkResponse {

    @Schema(description = "链路ID")
    private Long linkId;

    @Schema(description = "链路代码")
    private String linkCode;

    @Schema(description = "节点数量")
    private Integer nodeCount;
}


