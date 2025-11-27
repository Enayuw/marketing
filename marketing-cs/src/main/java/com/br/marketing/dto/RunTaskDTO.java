package com.br.marketing.dto;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName RunTaskDTO
 * @Author kongbx
 * @Date 2024/11/8 18:03
 */
@Data
public class RunTaskDTO extends PageSearchDTO {

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "依赖模板id")
    private Integer templateId;

}
