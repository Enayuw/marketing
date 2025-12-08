package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("链路详情响应")
public class LinkDetailResponse {

    @ApiModelProperty("链路信息")
    private LinkInfoVO linkInfo;

    @ApiModelProperty("节点列表")
    private List<LinkNodeDetailVO> nodes;
}


