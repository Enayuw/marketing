package com.br.marketing.dto;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResultPreviewDTO extends PageSearchDTO {
    @Schema(description = "任务id")
    @NotNull(message = "任务id不能为空")
    private Long taskId;
}
