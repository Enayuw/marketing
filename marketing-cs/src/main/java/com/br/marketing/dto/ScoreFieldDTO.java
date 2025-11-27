package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @ClassName ScoreFieldDTO
 * @Description 模型字段DTO
 * @Author kongbx
 * @Date 2024/9/21 16:41
 */
@Data
public class ScoreFieldDTO {

    @Schema(description = "模型名称")
    private String field;

    @Schema(description = "步长")
    private Integer step;

}
