package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName TagEffectiveDTO
 * @Author kongbx
 * @Date 2025/3/21 16:41
 */
@Data
public class TagEffectiveDTO {

    @Schema(description = "标签编码")
    private String tagCode;

    @Schema(description = "标签名称")
    private String tagName;

}
