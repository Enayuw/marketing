package com.br.marketing.dto;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SearchConditionDTO extends PageSearchDTO {
    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "状态 1-开始；2-关闭")
    private Integer status;

    @Schema(description = "规则名称")
    private String name;
}
