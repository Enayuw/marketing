package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 链路列表查询请求
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@ApiModel("链路列表查询请求")
public class LinkListRequest {

    @ApiModelProperty("链路代码（模糊查询）")
    private String linkCode;

    @ApiModelProperty("链路名称（模糊查询）")
    private String linkName;

    @ApiModelProperty("apiCode")
    private String apiCode;

    @ApiModelProperty("状态（0-禁用 1-启用）")
    private Integer status;

    @ApiModelProperty("页码")
    private Integer pageNum;

    @ApiModelProperty("每页大小")
    private Integer pageSize;
}


