package com.br.marketing.dto;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResultPreviewDTO extends PageSearchDTO {
    @ApiModelProperty(value = "任务id")
    @NotNull(message = "任务id不能为空")
    private Long taskId;
}
