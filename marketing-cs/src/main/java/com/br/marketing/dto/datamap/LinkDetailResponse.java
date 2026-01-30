package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 链路详情响应
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@Schema(description = "链路详情响应")
public class LinkDetailResponse {

    @Schema(description = "链路信息")
    private LinkInfoVO linkInfo;

    @Schema(description = "节点列表")
    private List<LinkNodeDetailVO> nodes;

    @Schema(description = "边列表")
    private List<LinkEdgeDetailVO> edges;
}


