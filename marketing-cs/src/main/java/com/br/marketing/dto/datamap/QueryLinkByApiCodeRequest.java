package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 根据apiCode查询链路详情列表请求
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@ApiModel("根据apiCode查询链路详情列表请求")
public class QueryLinkByApiCodeRequest {

    @NotNull(message = "apiCode不能为空")
    @ApiModelProperty(value = "apiCode", required = true)
    private String apiCode;

    @ApiModelProperty(value = "开始日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String startDate;

    @ApiModelProperty(value = "结束日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String endDate;

}

