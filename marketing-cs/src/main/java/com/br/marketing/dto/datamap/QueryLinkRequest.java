package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询链路详情
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@ApiModel("查询链路详情")
public class QueryLinkRequest {

    @NotNull(message = "链路ID不能为空")
    @ApiModelProperty(value = "链路id", required = true)
    private Long linkId;

    @ApiModelProperty(value = "开始日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String startDate;

    @ApiModelProperty(value = "结束日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String endDate;

}


