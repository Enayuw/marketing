package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * 创建链路请求
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Schema(description = "创建链路请求")
public class CreateLinkRequest {

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "链路id")
    private Long linkId;

    @Schema(description = "链路名称")
    private String linkName;

    @Schema(description = "业务场景")
    private String bizScene;

    @Schema(description = "链路描述")
    private String description;

    @Schema(description = "链路图结构（JSON格式，包含节点、连线、位置等完整信息）")
    private String graphJson;

    @Schema(description = "节点列表")
    @Valid
    private List<LinkNodeVO> nodes;
}


