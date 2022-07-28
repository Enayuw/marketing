package com.br.marketing.dto;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SearchConditionDTO extends PageSearchDTO {
    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "状态 1-开始；2-关闭")
    private Integer status;

    @ApiModelProperty(value = "规则名称")
    private String name;
}
