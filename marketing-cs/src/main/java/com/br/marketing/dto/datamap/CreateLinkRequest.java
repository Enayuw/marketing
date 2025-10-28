package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("创建链路请求")
public class CreateLinkRequest {

    @ApiModelProperty("apiCode")
    private String apiCode;

    @ApiModelProperty("链路id")
    private Long linkId;

    @ApiModelProperty("链路名称")
    private String linkName;

    @ApiModelProperty("业务场景")
    private String bizScene;

    @ApiModelProperty("链路描述")
    private String description;

    @ApiModelProperty("链路图结构（JSON格式，包含节点、连线、位置等完整信息）")
    private String graphJson;

    @ApiModelProperty("节点列表")
    @Valid
    private List<LinkNodeVO> nodes;
}


