package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("链路列表项")
public class LinkListItemVO {

    @ApiModelProperty("链路ID")
    private Long id;

    @ApiModelProperty("apiCode")
    private String apiCode;

    @ApiModelProperty("链路代码")
    private String linkCode;

    @ApiModelProperty("链路名称")
    private String linkName;

    @ApiModelProperty("业务场景")
    private String bizScene;

    @ApiModelProperty("链路描述")
    private String description;

    @ApiModelProperty("状态（0-禁用 1-启用）")
    private Integer status;

    @ApiModelProperty("节点数量")
    private Integer nodeCount;

    @ApiModelProperty("创建时间")
    private Date createdTime;

    @ApiModelProperty("更新时间")
    private Date updatedTime;
}


