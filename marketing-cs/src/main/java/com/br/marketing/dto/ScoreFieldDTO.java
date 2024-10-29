package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @ClassName ScoreFieldDTO
 * @Description 模型字段DTO
 * @Author kongbx
 * @Date 2024/9/21 16:41
 */
@Data
public class ScoreFieldDTO {

    @ApiModelProperty(value = "模型名称")
    private String field;

    @ApiModelProperty(value = "步长")
    private Integer step;

}
