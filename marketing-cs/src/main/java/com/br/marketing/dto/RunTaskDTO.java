package com.br.marketing.dto;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName RunTaskDTO
 * @Author kongbx
 * @Date 2024/11/8 18:03
 */
@Data
public class RunTaskDTO extends PageSearchDTO {

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "依赖模板id")
    private Integer templateId;

}
