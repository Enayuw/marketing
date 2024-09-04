package com.br.marketing.vo.bi.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * BI报表查询参数
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@ApiModel(value = "BI报表配置字典请求参数")
public class BiReportConfigDIctParam {

    @ApiModelProperty(value = "字典key")
    private String dictKey;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "字典值")
    private String dictValue;

    @ApiModelProperty(value = "字典描述")
    @JsonProperty(value = "dictDesc")
    private String dictDesc;

    @ApiModelProperty("1-有效；9-无效")
    private Integer isDel;
}
